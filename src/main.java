import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class main {

	
/**
 * -Creates a list of addresses and corresponding elevations from command-line arguments or use default values
 * -Sorts the list in descending order according to elevations
 * -Prints the list
 * 
 * @param args Options array of addresses / locations as strings
 */
	
 public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		String[] addresses;
		
		
		if (args.length != 0) {	// Sets default values if no Command line arguments are entered
			addresses = args;
		} else {
			addresses = new String[] { "1675 Larimer St, Denver, CO" , "33 E Quay Rd, Key West, FL 33040" , "630 Williams St NW, Atlanta, GA" };
		}

		AddressElevation[] elevations = new AddressElevation[addresses.length]; 
		elevations = getElevations(addresses);	
		sortAddresses(elevations);
			
		
		for (AddressElevation ae : elevations) {
			System.out.println(ae.toString());
		}

	}
 
 
/**
 * -Creates and returns an Array of AddressElevation Objects from an array of strings called elevations
 * -uses each address in addresses to first find latitude and longitude of address using HttpURLConnection GET method
 * -read in JSON object retrieved from GET method as String	
 * -isolate and save latitude longitude information as string 'latLong'
 * -parse 'latLong' as double
 * -use latLong to retrieve elevation information for address using HttpURLConnection GET method
 * -create and add new AddressElevation object to elevations
 * 
 * @param addresses An array of Strings formated as some location (Google seems to choose a location regardless if one is real or if address is exact)
 * @return An array of AddressElevation objects
 */
	public static AddressElevation[] getElevations(String[] addresses) {
		
		
		AddressElevation[] elevations = new AddressElevation[addresses.length];
		
		for (int i = 0; i < addresses.length; i++) {
		
			String param = addresses[i].replace(' ', '+');
			
		HttpURLConnection conn = null;
		String locKey = "&key=AIzaSyCDrCHbovMPcW-qQJ7s72rvZLLFW88F9dc";
		String eleKey = "&key=AIzaSyB7cof_KOFXIwBV9flPOKzcbmc6sac9NUc";
		String latLong;

		try {
			URL locUrl = new URL("https://maps.googleapis.com/maps/api/geocode/json?address=" + param + locKey);
			conn = (HttpURLConnection) locUrl.openConnection();
			conn.setRequestMethod("GET");
			conn.setDoOutput(true);
			DataOutputStream out = new DataOutputStream(conn.getOutputStream());
			out.writeBytes(addresses[i]);
			out.close();
			InputStream in = conn.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			StringBuffer response = new StringBuffer();
			String line;
			while ((line = br.readLine()) != null) {
				if (line.trim().equals("\"location\" : {")) {
					line = br.readLine();
					response.append(line.trim().substring(8));
					line = br.readLine();
					response.append(line.trim().substring(8));
				}
			}

			br.close();

			latLong = response.toString();
		
		} catch (Exception e) {
			
			latLong = "0,0";

		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}

		try {
			URL eleUrl = new URL("https://maps.googleapis.com/maps/api/elevation/json?locations=" + latLong + eleKey);
			conn = (HttpURLConnection) eleUrl.openConnection();
			conn.setRequestMethod("GET");
			conn.setDoOutput(true);
			DataOutputStream out = new DataOutputStream(conn.getOutputStream());
			out.writeBytes(addresses[i]);
			out.close();
			InputStream in = conn.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = br.readLine()) != null) {
				if (line.matches(".*?\\belevation\\b.*?")) {
					// removes extra comma
					if(i == addresses.length - 1)
						elevations[i] = new AddressElevation(addresses[i], line.trim().substring(14, line.trim().length() - 1));
					else
						elevations[i] = new AddressElevation(addresses[i].substring(0, addresses[i].length() - 1), line.trim().substring(14, line.trim().length() - 1));
					
					
				}

			}

			br.close();

		

		} catch (Exception e) {

			elevations[i] = new AddressElevation("Unknown Location",
					"0");


		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
		}
		
		return elevations;
	}
	
	/**
	 * -Sorts list of AddressElevation Objects in descending order according to elevation
	 * -Uses Selection Sort algorithm (chosen because of small list size)
	 * 
	 * @param ae Unsorted list
	 */

	public static void sortAddresses(AddressElevation[] ae) {

		int size = ae.length;
		AddressElevation temp;

		for (int i = 0; i < size - 1; i++) {
			int max = i;
			for (int j = i + 1; j < size; j++) {
				if (ae[j].getElevation() > ae[max].getElevation()) {
					max = j;				
				}
				temp = ae[max];
				ae[max] = ae[i];
				ae[i] = temp;
			}
		}
	}
}


/**
 * AddressElevqation holds two read only properties
 * - address: String 
 * - elevation: double
 */
class AddressElevation {

	private String address;
	private double elevation;

	public AddressElevation(String add, String ele) {
		address = add;

		elevation = Double.parseDouble(ele);

	}



	public double getElevation() {
		return elevation;
	}

	@Override
	public String toString() {
		return address + " , " + elevation;
	}

}
