import numpy as np
import dcop_instance as dcop
import sys, getopt, os
import itertools
import networkx as nx

def read_uai(fname):
    with open(fname) as f:
        content = f.readlines()
    content = [x.strip() for x in content]

    agts = {}
    vars = {}
    doms = {'0': list(range(0, 2))}
    cons = {}


    n_vars = 0
    n_con = 0

    # Read the number of variables
    L = content[1].split()
    n_vars = int(L[0]);

    # Read the domain cardinality
    L = content[2].split()
    for vid in range(n_vars):
        if int(L[vid]) != 2:
            print("Domain size must be 2")
            exit(-1)
        agts[str(vid)] = None
        vars[str(vid)] = {'dom': '0', 'agt': str(vid)}

    # Read number of cliques (constraints)
    L = content[3].split()
    n_con = int(L[0])

    offset = 4
    for cid in range(n_con):
        L = content[offset].split()
        arity = int(L[0])
        scope = [str(k) for k in L[1:]]
        offset += 1
        cons[str(cid)] = {'arity': arity, 'def_cost': 99999,
                          'scope': scope,
                          'values': []}

    for cid in range(n_con):
        L = []
        while not L:
            L = content[offset].split()
            values = [float(i) for i in content[offset+1].split()]
            offset += 1
        n = int(L[0])
        z = sum(values)
        values = [-np.log(x / z) if x > 0 else 0 for x in values]
        pset= itertools.product(*([[0, 1], ] * cons[str(cid)]['arity']))

        for i, tuple in enumerate(pset):
            cons[str(cid)]['values'].append({'tuple': tuple, 'cost': values[i]})

        offset += 1
    return agts, vars, doms, cons



def is_connected(vars, cons):
    G = nx.Graph()
    for v in vars:
        G.add_node(int(v))
    for e in cons:
        s = [int(x) for x in cons[e]['scope']]
        if len(s) > 1:
            for i in range(len(s)):
                for j in range(i+1, len(s)):
                    G.add_edge(s[i], s[j])


    return nx.is_connected(G)
    # if not nx.is_connected(G):
    #     graphs = list(nx.connected_component_subgraphs(G))
    #     for g in graphs:
    #         print("-----------------")
    #         print ("GRAPH", g.nodes())
    #     return False
    # return True




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
    uai_file,  outfile = main(sys.argv[1:])
    agts, vars, doms, cons = read_uai(uai_file)

    if not is_connected(vars, cons):
        print("Graph is not connected")
        exit(-1)

    name = 'UAI'
    print(len(vars))
    dcop.create_xml_instance(name, agts, vars, doms, cons, outfile+'.xml')
    dcop.create_wcsp_instance(name, agts, vars, doms, cons, outfile+'.wcsp')
    dcop.create_json_instance(name, agts, vars, doms, cons, outfile+'.json')