import org.apache.accumulo.core.client.ClientConfiguration;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Instance;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.accumulo.core.client.security.tokens.PasswordToken;
import org.apache.commons.configuration.BaseConfiguration;

import org.apache.rya.accumulo.AccumuloRdfConfiguration;
import org.apache.rya.rdftriplestore.RdfCloudTripleStore;

import org.apache.accumulo.core.client.ClientConfiguration;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Instance;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.accumulo.core.client.security.tokens.PasswordToken;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Range;
import org.apache.accumulo.core.data.Value;
import org.apache.commons.configuration.BaseConfiguration;

import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Instance;
//import org.apache.accumulo.core.client.mock.MockInstance;
import org.openrdf.model.Resource;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.text.tsv.SPARQLResultsTSVWriter;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

import org.apache.rya.accumulo.AccumuloRdfConfiguration;
import org.apache.rya.accumulo.AccumuloRyaDAO;
import org.apache.rya.rdftriplestore.RdfCloudTripleStore;
import org.apache.rya.rdftriplestore.RyaSailRepository;
import org.apache.rya.rdftriplestore.inference.InferenceEngine;
import org.apache.rya.rdftriplestore.namespace.NamespaceManager;

import org.apache.accumulo.core.Constants;

import org.apache.accumulo.core.client.ClientConfiguration;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Instance;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.accumulo.core.client.security.tokens.PasswordToken;
import org.apache.commons.configuration.BaseConfiguration;

//import org.apache.accumulo.core.client.mock.MockInstance;
import org.apache.rya.sail.config.RyaSailFactory;
import org.openrdf.sail.Sail;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collections;

public class Rdf4jApi {
    public static void main(String[] args) {
        final RdfCloudTripleStore store = new RdfCloudTripleStore();
        AccumuloRdfConfiguration conf = new AccumuloRdfConfiguration();
        AccumuloRyaDAO dao = new AccumuloRyaDAO();
        try {
            final BaseConfiguration apacheConf = new BaseConfiguration();
            apacheConf.setProperty("instance.name", "knoldus");
            apacheConf.setProperty("instance.zookeeper.host", "192.168.1.5:2181");
            //final ClientConfiguration aconf = new ClientConfiguration(Collections.singletonList(apacheConf));
            //final Instance instance = new ZooKeeperInstance(aconf);
            //Connector connector = instance.getConnector("jouko", "knoldus123");
            //Instance instance=new Instance("knoldus")
            //
            //final Instance instance = new ZooKeeperInstance(aconf);
            //Configuration.Accumulo accumuloConf = conf.getAccumulo();
            //Connector connector = new ZooKeeperInstance("knoldus", "192.168.1.5:2181").getConnector("jouko", "knoldus123");
            //Connector connector = new Instance("knoldus", "192.168.1.5:2181").getConnector("jouko", "knoldus123");
            //dao.setConnector(connector);
            conf.setTablePrefix("rya_");
            conf.setAccumuloInstance("knoldus");
            conf.setAccumuloPassword("knoldus123");
            conf.setAccumuloUser("jouko");
            conf.setAccumuloZookeepers("192.168.1.5:2181");
            //conf.setAuths("password");
            final Sail extSail = RyaSailFactory.getInstance(conf);
            SailRepository repository = new SailRepository(extSail);
            repository.initialize();
            SailRepositoryConnection conn = repository.getConnection();
            /*
            dao.setConf(conf);
            store.setRyaDAO(dao);

            Repository myRepository = new RyaSailRepository(store);
            myRepository.initialize();
            RepositoryConnection conn = myRepository.getConnection();
            */
//load data from file
            final File file = new File("ntriples.ntrips");
            conn.add(new FileInputStream(file), file.getName(),
                    RDFFormat.NTRIPLES, new Resource[]{});

            conn.commit();

            conn.close();
            //myRepository.shutDown();
            repository.shutDown();
        }
        //Connector connector = new ZooKeeperInstance("instance", "zoo1,zoo2,zoo3").getConnector("user", "password");
        catch (Exception e){
            System.out.println("Exception");
            e.printStackTrace();
        }

    }
}
