package ingenious.alerts.manager;


import java.io.IOException;

import ingenious.alerts.manager.rules.CheckComplexRule;
import ingenious.utils.ConfigsLoader;

public class AlertsManager {
	
	static ConfigsLoader configInstance;
	
	static {
		configInstance = ConfigsLoader.getInstance();
		configInstance.loadProperties();
	}
	
	public void validate() {
		//check all kind of rules
		try {
			new CheckComplexRule().detectChanges();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main (String[] args) {
		new AlertsManager().validate();
	}
}
