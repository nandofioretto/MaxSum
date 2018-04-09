import numpy as np
import pandas as pd
import networkx as nx
from dcop_gen_fednet import construct_federated_graph
from dcop_gen_fednet import generate
import dcop_instance as dcop

path = '/home/fioretto/Repos/datasets/HiggsTwitter/'
file = 'higgs-reply_network.edgelist'

data = pd.read_csv(path+file, sep=' ', header=None, dtype=int)
data = data.rename(columns={0: 'u', 1: 'v', 2: 'w'})
nodes = list( set(data.v.values).union(set(data.u.values)))
nodes.sort()
node_map = {x: i for i, x in enumerate(nodes)}
N, E = len(nodes), len(data)
nodes = list(range(N))
data.u = [node_map[u] for u in data.u.values]
data.v = [node_map[v] for v in data.v.values]

Gtweet = nx.Graph()
Gtweet.add_nodes_from(nodes)
for i, row in data.iterrows():
    u,v,w = row
    Gtweet.add_edge(u, v)
Gtweet.to_directed()
print(Gtweet.number_of_nodes(), Gtweet.number_of_edges())



for nagts in [50, 100, 500, 1000]:
    for seed in range(10):
        G = construct_federated_graph(nagts, Gtweet, seed=seed)
        print('Nodes:', len(G.nodes()), ' Edges:', len(G.edges()), '/',
              (len(G.nodes()) * (len(G.nodes()) - 1)) / 2, ' :',
              len(G.edges()) / ((len(G.nodes()) * (len(G.nodes()) - 1)) / 2))

        assert nx.number_connected_components(G) == 1, \
            'number of connected components: ' + str(
                nx.number_connected_components(G))

        agts, vars, doms, cons = generate(G, cost_range=(0, 10))
        outfile = '/home/fioretto/Repos/MaxSum/scripts/data/tweet_graph_' + \
                  str(nagts) + '_' + str(seed)
        name = 'tweet_'+ str(nagts) + '_' + str(seed)
        print('Creating DCOP instance' + name, ' G nodes: ', len(G.nodes()),
              ' G edges:', len(G.edges()))

        dcop.create_xml_instance(name, agts, vars, doms, cons, outfile+'.xml')
        dcop.create_wcsp_instance(name, agts, vars, doms, cons, outfile+'.wcsp')
        #dcop.create_json_instance(name, agts, vars, doms, cons, outfile+'.json')