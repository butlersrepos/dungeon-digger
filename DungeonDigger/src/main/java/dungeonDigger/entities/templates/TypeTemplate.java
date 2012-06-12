package dungeonDigger.entities.templates;

import dungeonDigger.entities.Agent;

public abstract class TypeTemplate {
	private Agent host;
	public abstract void applyTo(Agent host);
	
	public void setHost(Agent host) {
		this.host = host;
	}
	public Agent getHost() {
		return host;
	}
}
