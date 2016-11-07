package dk.kb.webdanica.core.datamodel.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

import dk.kb.webdanica.core.datamodel.Cassandra;
import dk.kb.webdanica.core.datamodel.DanicaStatus;
import dk.kb.webdanica.core.datamodel.Database;
import dk.kb.webdanica.core.datamodel.Seed;
import dk.kb.webdanica.core.datamodel.Status;

/**
 * DAO class for the seeds table.
 * @author svc
 *
 */
public class CassandraSeedDAO implements SeedsDAO {
		
	static CassandraSeedDAO instance;
	
	private Database db;

	private Session session;
	private PreparedStatement preparedInsert;
	
	private PreparedStatement readAllWithStatestatement;
	
	private PreparedStatement readAllWithStateAndLimitstatement;

	private PreparedStatement preparedUpdateState;
	
	private PreparedStatement getSeedsCountStatement;
	
	private boolean newSession = false;

	private PreparedStatement preparedUpdateRedirectedUrl;
	
	public synchronized static CassandraSeedDAO getInstance(){
		if (instance == null) {
			instance = new CassandraSeedDAO();
		} 
		return instance;
	}
	
	public CassandraSeedDAO() {
		CassandraSettings settings = CassandraSettings.getDefaultSettings();
		db = new Cassandra(settings);
	}
	
	public List<Seed> getSeeds(Status status, int limit) {
		init();
		BoundStatement bStatement = readAllWithStateAndLimitstatement.bind(status.ordinal(), limit);
		ResultSet results = session.execute(bStatement);
		List<Seed> seedList = new ArrayList<Seed>();
		
		for (Row row: results.all()) {
			Seed s = getSeedFromRow(row);
			seedList.add(s);
		}
		return seedList; 
	}	
	
	public List<Seed> getSeeds(Status status) {
		init();
		BoundStatement bStatement = readAllWithStatestatement.bind(status.ordinal());
		ResultSet results = session.execute(bStatement);
		List<Seed> seedList = new ArrayList<Seed>();
		
		for (Row row: results.all()) {
			Seed s = getSeedFromRow(row);
			seedList.add(s);
		}
		return seedList; 
	}	

	private Seed getSeedFromRow(Row row) {
		//Seed(String url, String redirectedUrl, String hostname, String domain, String tld, Long insertedTime, Long updatedTime, DanicaStatus danicastate, 
		//		Status state, String stateReason) 
		return new Seed(
				row.getString("url"),
				row.getString("redirected_url"), 
				row.getString("host"),
				row.getString("domain"),
				row.getString("tld"),
				row.getLong("inserted_time"),
				row.getLong("updated_time"),
				DanicaStatus.fromOrdinal(row.getInt("danica")),
				Status.fromOrdinal(row.getInt("status")), 
				row.getString("status_reason")
				);
    }

	public boolean insertSeed(Seed singleSeed) {
		init();	
		Date insertedDate = new Date();
		BoundStatement bound = preparedInsert.bind(singleSeed.getUrl(), singleSeed.getStatus().ordinal(), insertedDate);
		ResultSet rs = session.execute(bound);
		Row row = rs.one();
		boolean insertFailed = row.getColumnDefinitions().contains("url");
		return !insertFailed; // Was the insert successful?
	}

	private void init() {
		if (session == null || session.isClosed()) {			
			session = db.getSession();
			newSession = true;
		}
		if (preparedInsert == null || newSession) {
			//FIXME This is no longer valid 
			preparedInsert = session.prepare("INSERT INTO seeds (url, status, inserted_time) VALUES (?,?,?) IF NOT EXISTS");
		}
		
		if (preparedUpdateState == null || newSession) {
			preparedUpdateState = session.prepare("UPDATE seeds SET status=?, status_reason=? WHERE url=? IF EXISTS");
		}
		if (readAllWithStatestatement == null || newSession) {
			readAllWithStatestatement = session.prepare("SELECT * FROM seeds WHERE status=?");
		}
		if (readAllWithStateAndLimitstatement == null || newSession) {
			readAllWithStateAndLimitstatement = session.prepare("SELECT * FROM seeds WHERE status=? LIMIT ?");
		}
		
		if (getSeedsCountStatement == null || newSession) {
			getSeedsCountStatement = session.prepare("SELECT count(*) FROM seeds WHERE status=?");
		}
		
		if (preparedUpdateRedirectedUrl == null || newSession) {
			preparedUpdateRedirectedUrl = session.prepare("UPDATE seeds SET redirected_url=? WHERE url=? IF EXISTS");
		}
		newSession = false;
    }
	
	public boolean updateState(Seed singleSeed) {
		init();
		BoundStatement bound = preparedUpdateState.bind(singleSeed.getStatus().ordinal(), 
				singleSeed.getStatusReason(), singleSeed.getUrl());
		ResultSet rs = session.execute(bound);
		Row row = rs.one();
		// Possibly change this code. this depends on appending IF EXISTS to the statement
		boolean updateFailed = row.getColumnDefinitions().contains("url");
		return !updateFailed;
	}
	
	public Long getSeedsCount(Status status) {
		init();
		BoundStatement bStatement = getSeedsCountStatement.bind(status.ordinal());
		ResultSet rs = session.execute(bStatement);
		Row row = rs.one();
		return new Long(row.getLong(0));
	}	
	

	public void close() {
	    db.close();
    }

	public boolean updateRedirectedUrl(Seed s) {
	    init();
	    BoundStatement bound = preparedUpdateRedirectedUrl.bind(s.getRedirectedUrl(), s.getUrl()); 
		ResultSet rs = session.execute(bound);
		Row row = rs.one();
		// Possibly change this code. this depends on appending IF EXISTS to the statement
		boolean updateFailed = row.getColumnDefinitions().contains("url");
		return !updateFailed;
    }

	@Override
    public Long getSeedsDanicaCount(DanicaStatus s) {
	    // TODO Auto-generated method stub
	    return null;
    }
}
