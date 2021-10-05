package ingenious.utils;

import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.impl.MutableTupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;

public class QueryUtils {
	 public static TupleQueryResult evaluateSelectQuery(RepositoryConnection connection, String query,
             Binding... bindings)
            		 throws MalformedQueryException, RepositoryException, QueryEvaluationException {
		 // Preparing a new query
		 TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);

		 // Setting any potential bindings (query parameters)
		 for (Binding b : bindings) {
			 tupleQuery.setBinding(b.getName(), b.getValue());
		 }

		 // Sending the query to GraphDB, evaluating it and returning the result
		 return tupleQuery.evaluate();
	 }
	 
	 public static TupleQueryResult evaluateSelectQuery2(RepositoryConnection connection, String query,
             Binding... bindings)
            		 throws MalformedQueryException, RepositoryException, QueryEvaluationException {
		 // Preparing a new query
		 TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);

		 // Setting any potential bindings (query parameters)
		 for (Binding b : bindings) {
			 tupleQuery.setBinding(b.getName(), b.getValue());
		 }
		
		 TupleQueryResult result = new MutableTupleQueryResult(tupleQuery.evaluate());
		 return result;
	 }
}
