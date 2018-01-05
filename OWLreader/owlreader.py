# from owlready import *
import pronto

from ontobio.ontol_factory import OntologyFactory


class OntologyReader:

    def loadOntology(self):

        # onto_path.append("/Users/dwelter/Development/Sandbox/OWLreader/HCAO_v2.owl")
        # # onto = get_ontology("https://raw.githubusercontent.com/HumanCellAtlas/ontology/master/hcao.owl").load()
        # onto = load_ontology_from_file("/Users/dwelter/Development/Sandbox/OWLreader/HCAO_v2.owl")
        #
        # print("Ontology loaded")
        #
        # print(onto.base_iri)


        ont = pronto.Ontology("/Users/dwelter/Development/Sandbox/OWLreader/HCAO_v2.owl")
        done = False

        while not done:
            for term in ont:
                # if term.id == 'CL:0000000':
                if term.id == 'CL:0000595':
                    cell = term
                    done = True

        cell.id
        cell.name
        cell.children


    def loadInOWL(self):


        ofactory = OntologyFactory()
        ont = ofactory.create("/Users/dwelter/Development/Sandbox/OWLreader/HCAO_v2.owl")

        print(ont)




if __name__ == '__main__':
    reader = OntologyReader()

    # reader.loadOntology()
    reader.loadInOWL()