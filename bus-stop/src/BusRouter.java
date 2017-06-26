import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import smartcity.gtfs.*;
import smartcity.util.*;

public class BusRouter {

	private Double latOrigin = -30.0480764; 
	private Double longOrigin = -51.2284918;
	private Double latDestiny = -30.0616371;
	private Double longDestiny = -51.1575284;
	
	private Map<String,Stop> stops;
	private Map<String,Trip> trips;
	private Scanner scan;
	
	// Construtor passando Stops e Trips
	public BusRouter(Map<String,Stop> stops, Map<String,Trip> trips) {
		this.stops = stops;
		this.trips = trips;
		scan = new Scanner(System.in);
	}

	public void startNewRoute() {
//		System.out.println("Informe o quanto pretende caminhar (em metros. ex: 50): ");
//		Double walkDistance = scan.nextDouble();
		Double walkDistance = 250.0;
		
		GPSCoordinate originCoordinate = new GPSCoordinate(latOrigin, longOrigin);//askUserToCreateCoordinate(scan);
		GPSCoordinate destinyCoordinate = new GPSCoordinate(latDestiny, longDestiny);//askUserToCreateCoordinate(scan);
		getRouteFor(originCoordinate, destinyCoordinate, walkDistance);
	}
	
	private void getRouteFor(GPSCoordinate origin, GPSCoordinate destiny, Double walkDistance) {
		List<Stop> originStops = closestStopsFrom(origin, stops, walkDistance);
		Stop destinyStop = closestStopFrom(destiny, stops);
		List<Trip> closestDestinyTrips = getClosestTrips(destinyStop.getGPSCoordinate(), walkDistance, trips);
		calculateStopSequence(originStops, destinyStop, closestDestinyTrips, walkDistance, origin);
	}
	
	private void calculateStopSequence(List<Stop> originStops, Stop destinyStop, List<Trip> closestDestinyTrips, Double threshold, GPSCoordinate origin) {
		boolean hasFoundTrip = getRoutForLines(originStops, closestDestinyTrips, threshold);
		if (hasFoundTrip) { 
			// Caso tenha encontrado a rota, finaliza (linha direta)
			System.out.println("FIM");
		} else { 
			// Caso não tenha encontrado a rota, é preciso calcular a troca de onibus
			getRoutForMultipleLines(origin, threshold, closestDestinyTrips, destinyStop);
		}
	}
	
	private boolean getRoutForLines(List<Stop> originStops, List<Trip> closestDestinyTrips, Double threshold){
		Boolean hasFoundTrip = false;
		for (Trip t : closestDestinyTrips) {
			for (Stop s : originStops) {
				// Caso tenha parada proxima a linha final, então é uma rota de 1 onibus apenas.
				if (t.hasStopNear(s.getGPSCoordinate(), threshold)) { 
					System.out.println("LINHA DO DESTINO: " + t.getRoute().getLongName() + "|" + t.getRoute().getShortName());
					System.out.println("PARADA: "+s.getName());
					hasFoundTrip = true;
				}
			}
			System.out.println("----------------------");
		}
		return hasFoundTrip;
	}
	
	private void getRoutForMultipleLines(GPSCoordinate origin, Double threshold, List<Trip> closestDestinyTrips, Stop destinyStop){
		// Pega a lista das linhas mais proximas da origem.
		List<Trip> closestOriginTrips = getClosestTrips(origin, threshold, trips); 
		
		// Varre a lista de todas as paradas
		for (Map.Entry<String, Stop> s : stops.entrySet()) { 
			Stop stop = s.getValue();
			// Para cada linha que passa no destino
			for (Trip destinyTrip : closestDestinyTrips) { 
				// Se a parada da vez estiver perto da linha do destino
				if (destinyTrip.hasStopNear(stop.getGPSCoordinate(), threshold)) { 
					// Para cada linha da origem
					for (Trip originTrip : closestOriginTrips) {
						// Se a parada estiver perto da linha da origem
						if (originTrip.hasStopNear(stop.getGPSCoordinate(), threshold)) { 
							// Pega a linha mais proxima da parada de origem.
							Stop originStop = closestStopFrom(origin, stops); 
							Trip finalTrip = closestDestinyTrips.get(closestDestinyTrips.size() - 1);
							System.out.println("PARTIDA NA LINHA " + originTrip.getRoute().getShortName() + " | PARADA: " + originStop.getName());
							System.out.println("DESCER NA PARADA: " + stop.getName());
							System.out.println("EMBARCAR NA LINHA: " + finalTrip.getRoute().getShortName());
							System.out.println("DESCER NA PARADA: " + destinyStop.getName());
							return;
						}
					}
				} 
			} 
		}
	}
	
	/* closestStopFrom: Dada uma coordenada e uma lista de paradas, retorna-se a 
	 	parada mais próxima desta coordenada */
	private Stop closestStopFrom(GPSCoordinate originCoordinate, Map<String,Stop> stops) {
		Stop closestStop = new Stop("","",0,0);
		Double maxValue = Double.MAX_VALUE;
		
		for(Map.Entry<String, Stop> stop : stops.entrySet()){
			GPSCoordinate coordParada = stop.getValue().getGPSCoordinate();
			Double stopsDistance = originCoordinate.distance(coordParada);
			if (stopsDistance < maxValue) {
				maxValue = stopsDistance;
				closestStop = stop.getValue();
			}
		}
		return closestStop;
	}
	
	/* closestStopsFrom: Dada uma coordenada e uma lista de paradas, retorna-se a lista das
 	paradas mais próximas desta coordenada.
 	LISTA TODAS AS PARADAS PRÓXIMAS A UMA COORDENADA */
	private List<Stop> closestStopsFrom(GPSCoordinate originCoordinate, Map<String,Stop> stops, Double threshold) {
		List<Stop> closestStops = new ArrayList<Stop>();
	
		for (Map.Entry<String, Stop> s : stops.entrySet()) {
			Stop stop = s.getValue();
			GPSCoordinate stopCoordinate = stop.getGPSCoordinate();
			Double stopDistance = originCoordinate.distance(stopCoordinate);
			if (stopDistance <= threshold) {
				closestStops.add(stop);
			}
		}
		return closestStops;
	}
	
	/* getClosestTrips: dada uma coordenada, um limite de caminhada e uma lista de trips,
	   retorna-se todas as trips que tem uma parada que esteja dentro do limite da caminhada.
	   LISTA TODAS AS LINHAS QUE PASSAM EM UMA DETERMINADA COORDENADA. */
	private List<Trip> getClosestTrips(GPSCoordinate coord, Double threshold, Map<String,Trip> trips) {
		List<Trip> closestTrips = new ArrayList<Trip>();
		List<String> closestTripNames = new ArrayList<String>();
		for (Map.Entry<String, Trip> t : trips.entrySet()) {
			Trip trip = t.getValue();
			if (trip.hasStopNear(coord, threshold) && !closestTripNames.contains(trip.getRoute().getShortName())) {
				closestTrips.add(t.getValue());
				closestTripNames.add(t.getValue().getRoute().getShortName());
			}
		}
		return closestTrips;
	}	
	
	/* askUserToCreateCoordinate: Pede ao usuário que informe uma latitude e uma longitude, 
	   a fim de criar uma coordenada. Ao final da funcão, retorna-se a coordenada. */
	private GPSCoordinate askUserToCreateCoordinate(Scanner scan) {
		System.out.println("Digite a latitude: ");
		Double lat = scan.nextDouble();
		System.out.println("Digite a longitude: ");
		Double lng = scan.nextDouble();
		return new GPSCoordinate(lat, lng);
	}
}
