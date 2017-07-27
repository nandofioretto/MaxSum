import numpy as np
import json
import sys, getopt, os


## Commons I/O
def makedir(path):
    if not path or os.path.exists(path):
        return []
    (head, tail) = os.path.split(path)
    res = makedir(head)
    if not os.path.exists(path):
        os.mkdir(path)
    res += [path]
    return res


def load_json_file(json_file: str) -> object:
    """ Loads a json file into a dictionary
    :rtype: dict
    """
    with open(json_file) as data_file:
        data = json.load(data_file)
    return data


def save_json_path_file(path: str, filename: str, data: dict):
    """ Write a dictionary as a json file """
    makedir(path)
    if not path.endswith('/'):
        path += '/'
    print('saving Json file: ', filename, ' in path: ', path)
    with open(path + filename, 'w') as outfile:
        json.dump(data, outfile, indent=2)


def save_json_file(pathfile: str, data: dict):
    """ Write a dictionary as a json file """
    path, filename = os.path.split(pathfile)
    save_json_path_file(path, filename, data)


def read_ccg(fname):
    with open(fname) as f:
        content = f.readlines()
    content = [x.strip() for x in content]

    vars = {}
    edges = {}
    read_vtype = False
    read_assignment = False

    for i, line in enumerate(content):
        L = line.split()
        if L[0] == 'v':
            vars[int(L[1])] = {'weight': float(L[2]), 'type': None, 'val': None, 'con': [],
                               'id': int(L[1])}
        elif L[0] == 'e':
            edges[i] = []
            edges[i].append(int(L[1]))
            edges[i].append(int(L[2]))
            vars[int(L[1])]['con'].append(i)
            vars[int(L[2])]['con'].append(i)

        if 'vertex types end' in line:
            read_vtype = False
        elif 'assignments end' in line:
            read_assignment = False

        if read_vtype:
            vars[int(L[0])]['type'] = int(L[1])
        elif read_assignment:
            vars[int(L[0])]['val'] = int(L[1])

        if 'vertex types begin' in line:
            read_vtype = True
        elif 'assignments begin' in line:
            read_assignment = True

    return vars, edges


def is_in_kernel(var):
    return var['val'] is None


def make_wcsp(vars, edges):

    unary = {}
    edges_to_remove = []
    vars_to_remove = []

    for vi in vars:
        vi_val = vars[vi]['val']

        # If vi's value was set (NOT IN THE KERNEL)
        if not is_in_kernel(vars[vi]):
            # For each binary constraint c involving vi and some vj in the KERNEL,
            # transform c into a unary constraint.
            for cidx in vars[vi]['con']:

                v1, v2 = edges[cidx]
                vj = v2 if v1 == vi else v1
                vj_val = vars[vj]['val']

                if is_in_kernel(vars[vj]):
                    w0 = np.inf if (vi_val == 0) else 0
                    w1 = 0
                    if vj not in unary:
                        unary[vj] = [w0, w1]
                    else:
                        unary[vj][0] += w0
                        unary[vj][1] += w1

                elif vi_val == 0 and vj_val == 0:
                    print('Problem is UNSAT')
                    exit(-2)

                edges_to_remove.append(cidx)
                vars_to_remove.append(vi)
                # v not in kernel

    # for v in vars_to_remove:
    #     del vars[v]
    # for c in edges_to_remove:
    #     del edges[c]

    for v in vars:
        if v in unary:
            unary[v][1] += vars[v]['weight']
        else:
            unary[v] = [0.0, vars[v]['weight']]

    binary = {}
    for c in edges:
        binary[c] = [np.inf, 0.0, 0.0, 0.0]

    wcsp = {'variables': {},
            'constraints': {}}


    for v in vars:
        wcsp['variables']['v_' + str(v)] = {
            'id': vars[v]['id'],
            'domain': [0, 1],
            'agent' : 'a_' + str(v) if vars[v]['type'] >= 0 else None,
            'value' : None,
            'type'  : vars[v]['type'],
            'cons'  : []
        }

    c_id = 0
    for u in unary:
        vname = 'v_'+str(u)
        cname = 'c_'+str(c_id)
        wcsp['constraints'][cname] = {'scope': [vname], 'vals' : unary[u]}
        wcsp['variables'][vname]['cons'].append(cname)
        c_id += 1

    for b in binary:
        v1name, v2name = 'v_'+str(edges[b][0]), 'v_'+str(edges[b][1])
        cname = 'c_'+str(c_id)
        wcsp['constraints'][cname] = {'scope': [v1name, v2name], 'vals': binary[b]}
        wcsp['variables'][v1name]['cons'].append(cname)
        wcsp['variables'][v2name]['cons'].append(cname)
        c_id+=1

    return wcsp


def make_dcop(wcsp):
    # Assign each aux variable u to the agent which is managing the decision variable v
    for v in wcsp['variables']:
        if wcsp['variables'][v]['type'] < 0:
            C = wcsp['variables'][v]['cons']
            for c in C:
                scope = wcsp['constraints'][c]['scope']
                
                min_id = scope[0]
                for u in scope:
                    if wcsp['variables'][u]['type'] >= 0 and u < min_id:
                        min_id = u

                wcsp['variables'][v]['agent'] = wcsp['variables'][min_id]['agent']

    wcsp['agents'] = {}
    aid = 0
    for v in wcsp['variables']:
        agt = wcsp['variables'][v]['agent']
        if agt not in wcsp['agents']:
            wcsp['agents'][agt] = {'id': aid, 'vars': []}
            aid += 1
        wcsp['agents'][agt]['vars'].append(v)

    for c in wcsp['constraints']:
        for i, val in enumerate(wcsp['constraints'][c]['vals']):
            if np.isinf(val):
                wcsp['constraints'][c]['vals'][i] = -9999.0

    return wcsp
    # connected to u with some constraint


def main(argv):
    in_file = ''
    out_file = ''
    def rise_exception():
        print('main.py -i <input> -o <outputfile>')
        sys.exit(2)

    try:
        opts, args = getopt.getopt(argv, "i:o:h", ["ifile=", "ofile=", "help"])
    except getopt.GetoptError:
        rise_exception()
    if len(opts) != 2:
        rise_exception()

    for opt, arg in opts:
        if opt in ('-h', '--help'):
            print('main.py -i <inputfile> -o <outputfile>')
            sys.exit()
        elif opt in ("-i", "--ifile"):
            in_file = arg
        elif opt in ("-o", "--ofile"):
            out_file = arg
    return in_file, out_file


if __name__ == '__main__':
    fname, outfile = main(sys.argv[1:])

    vars_dic, edges_dic = read_ccg(fname)
    wcsp = make_wcsp(vars_dic, edges_dic)
    # check if wcsp is solved:
    solved = True
    for v in wcsp['variables']:
        if wcsp['variables'][v]['type'] >= 0:
            solved = False
    if solved:
        print('Problem already solved')
        exit(1)
    else:
        dcop = make_dcop(wcsp)
        print('saving dcop: agents=', len(dcop['agents']), ' variables=', len(dcop['variables']), ' constraints=', len(dcop['constraints']))
        save_json_file(outfile, dcop)
