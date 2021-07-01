package ingenious.alerts.manager;


import ingenious.alerts.manager.rules.CheckComplexRule;

public class AlertsManager {
	
	
	
	public void validate() {
		//check all kind of rules
		new CheckComplexRule().start();
	}

}
