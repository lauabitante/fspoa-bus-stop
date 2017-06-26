import java.io.FileNotFoundException;
import java.util.Map;

import smartcity.gtfs.GTFSReader;
import smartcity.gtfs.Route;
import smartcity.gtfs.Service;
import smartcity.gtfs.Shape;
import smartcity.gtfs.Stop;
import smartcity.gtfs.Trip;

public class Main {
	
	static Map<String,Stop> stops;
	static Map<String,Trip> trips;

	public static void main(String[] args) throws FileNotFoundException {
		
		readAllFiles();
		
		BusRouter busRouter = new BusRouter(stops, trips);
		busRouter.startNewRoute();
	}
	
	/*
	 *  Método para leitura dos arquivos.
	 */
	public static void readAllFiles() throws FileNotFoundException {
		System.out.println("Reading stops.");
		stops = GTFSReader.loadStops("bin/data/stops.txt");
		
		System.out.println("Reading routes.");
		Map<String,Route>routes = GTFSReader.loadRoutes("bin/data/routes.txt");
		
		System.out.println("Reading shapes.");
		Map<String,Shape> shapes = GTFSReader.loadShapes("bin/data/shapes.txt");
		
		System.out.println("Reading calendar.");
		Map<String,Service> services = GTFSReader.loadServices("bin/data/calendar.txt");
		
		System.out.println("Reading trips.");
		trips = GTFSReader.loadTrips("bin/data/trips.txt",routes,services,shapes);
		
		System.out.println("Reading stop times.");
		long s = System.currentTimeMillis();
		GTFSReader.loadStopTimes("bin/data/stop_times.txt", trips, stops);
		long e = System.currentTimeMillis();
		System.out.println("\nTempo = " + ((e-s)/1000.0));
	}
}
