package com.crossvale.service;

import java.util.List;
import java.util.ArrayList;
import java.lang.Integer;

import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.crossvale.model.ClusterInput;
import com.crossvale.model.ClusterOutput;
import com.crossvale.model.FleetOut;
import com.crossvale.model.ClusterOut;
import com.crossvale.model.Cluster;
import com.crossvale.model.Fleet;

@Service
public class BalancerService {

	private final KieContainer kieContainer;

	@Autowired
	public BalancerService(KieContainer kieContainer) {        
        this.kieContainer = kieContainer;
	}

	
	public ClusterOutput createClusterOutput(ClusterInput clusterInput) {
		
		List<ClusterOut> clusterOutList = new ArrayList<>();
		DateTime dt = new DateTime(DateTimeZone.UTC);
	    int hhmm = dt.getHourOfDay()*100 + dt.getMinuteOfHour();
				
		for(Cluster cluster : clusterInput.getCluster()) {
			
			List<FleetOut> fleetOutList = new ArrayList<>();
			
			for(Fleet fleet : cluster.getFleet()) {
				
				FleetOut fleetOut = new FleetOut();				
				fleetOut.setCurrentCapacity(fleet.getCurrentCapacity());
				fleetOut.setId(fleet.getId());
				fleetOut.setCurrentTime(hhmm);
				fleetOut.setName(fleet.getName());
				fleetOut.setTargetCapacity(fleet.getCurrentCapacity());
				
				KieSession kieSession = kieContainer.newKieSession("rulesSession");
				//KieSession ksession = kbase.newKieSession();
				//kieSession.setGlobal("hhmm", hhmm);
				kieSession.insert(fleetOut);
				kieSession.insert(cluster);
				kieSession.fireAllRules();
				kieSession.dispose();
				
				fleetOutList.add(fleetOut);
				
			}

			ClusterOut clusterOut = new ClusterOut();
			clusterOut.setCloud(cluster.getCloud());
			clusterOut.setName(cluster.getName());
			clusterOut.setFleet(fleetOutList);
			
			clusterOutList.add(clusterOut);
		}
		
		ClusterOutput clusterOutput = new ClusterOutput(clusterOutList);
		

		return clusterOutput;
	}
}