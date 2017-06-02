package imageretrieval;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import com.google.gson.Gson;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;

public class HttpServer extends NanoHTTPD {
	
	public Searcher searcher = null;

	public HttpServer() throws IOException {
		super(8081);
		
		searcher = new Searcher();
		
		start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("\nRunning! Point your browsers to http://localhost:8081/ \n");
	}

	public static void main(String[] args) {
		try {
			new HttpServer();
		} catch (IOException ioe) {
			System.err.println("Couldn't start server:\n" + ioe);
		}
	}

	@Override
	public Response serve(IHTTPSession session) {
		String uri = session.getUri();
		Map<String, String> parms = session.getParms();
		Gson gson = new Gson();
		
		System.out.println(parms.get("filename"));
		
		String[] files = searcher.run(parms.get("filename"));
		Response res;
		if (files == null) {
			res = newFixedLengthResponse("IAS");
			res.setStatus(Status.BAD_REQUEST);
		} else {
			String msg = gson.toJson(files);
			res = newFixedLengthResponse(msg);
			res.setMimeType("application/json; charset=utf-8");
		}
		return res;
	}
	
	
}
