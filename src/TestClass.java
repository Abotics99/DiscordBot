import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

public class TestClass {
	
	public static void main(String[] args) {
		System.out.println(GetMemeUrl("r/dankmemes"));
	}
	
	private static String GetMemeUrl(String subreddit) {

		String url = "https://www.reddit.com/"+subreddit+".json?sort=top&t=week";
		
		try {
			String unparsedResponse = GetHttpData(url);
			
			JSONObject obj = new JSONObject(unparsedResponse);
			
			JSONArray objData = obj.getJSONObject("data").getJSONArray("children");
			
			Random random = new Random();
			
			return objData.getJSONObject(random.nextInt(objData.length())).getJSONObject("data").getString("url");
		} catch (Exception e) {
			
			e.printStackTrace();
			
			return "null";
		}
	}
	
	private static String GetHttpData(String url) throws Exception {
    	URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// optional default is GET
		con.setRequestMethod("GET");

		//add request header
		con.setRequestProperty("User-Agent", "Mozilla/5.0");

		int responseCode = con.getResponseCode();

		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		
		return response.toString();
    }
}
