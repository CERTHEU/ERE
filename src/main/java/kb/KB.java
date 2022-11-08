package kb;

import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.config.RepositoryConfigException;
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;

import ingenious.utils.ConfigsLoader;


public class KB {
	
	public static final String REPOSITORY = "Ing-SST4";
	 
	public RepositoryConnection connection;
	public ValueFactory factory;
	private RepositoryManager manager;
	 
	 public KB(String serverUrl) {
			manager = new RemoteRepositoryManager(serverUrl);
			((RemoteRepositoryManager)manager).setUsernameAndPassword("", "");
			manager.init();
	        connection = getRepositoryConnection(REPOSITORY);
	        factory = connection.getValueFactory();
	    }
	 
	 
	  private RepositoryConnection getRepositoryConnection(String repoName) {
	        Repository repository = manager.getRepository(repoName);
	        if (repository == null) {
	        	throw new RepositoryException("There is no repository with name : " + repoName);
	        }
	        return repository.getConnection();
	 }
	  
	  
	  public Repository getRepository(String id) throws RepositoryConfigException, RepositoryException {
			return manager.getRepository(id);
		}

		public boolean removeRepository(String id) throws RepositoryConfigException, RepositoryException {
			return manager.removeRepository(id);
		}

		public void shutDown(String CONTEXT) {
			System.out.println("closing GraphDb manager " + CONTEXT);
			if (manager != null) {
				try {
					manager.shutDown();
				} catch (Exception e) {
					System.err.println(e.getMessage());
				}
			}
		}

		public RepositoryManager getManager() {
			return manager;
		}
		
		public RepositoryConnection getConnection() {
			return connection;
		}

		public ValueFactory getFactory() {
			return factory;
		}
		
		public void shutDown() {
			System.out.println("closing GraphDb manager [ {}]\n");
			if (manager != null) {
				try {
					manager.shutDown();
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
			}
		}

}
