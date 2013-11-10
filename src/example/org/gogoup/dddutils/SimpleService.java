package example.org.gogoup.dddutils;

import org.gogoup.dddutils.appsession.AppSession;
import org.gogoup.dddutils.appsession.InconsistentAppSessionException;
import org.gogoup.dddutils.appsession.NoSuchAppSessionException;
import org.gogoup.dddutils.remote.server.RemoteHttpMethod;
import org.gogoup.dddutils.remote.server.RemoteHttpResponse;
import org.gogoup.dddutils.remote.server.impl.SimpleRemoteHttpService;

public class SimpleService extends SimpleRemoteHttpService {

	public SimpleService() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void process(RemoteHttpMethod method, AppSession session,
			RemoteHttpResponse response) {
		
		StringBuilder info = new StringBuilder();
		
		info.append("This is an simple http server!");
		info.append("\n\n");
		
		info.append("Session: ");
		info.append(session.getSessionKey());
		info.append("\n");
		
		info.append("Method: ");
		info.append(method);
		info.append("\n");
		
		info.append("Headers: {\n");
		String[] headerNames = this.getHttpHeaderNames();
		for(String name: headerNames)
		{
			info.append("    "+name);
			info.append(": [");
			for(String val: (String[])session.getParameter(name))
			{
				info.append(val);
				info.append(", ");
			}
			
			info.append("]\n");
		}
		info.append("}\n");
				
		info.append("Parameters: {\n");
		String[] parameterNames = this.getQueryParameterNames();
		for(String name: parameterNames)
		{
			info.append("    "+name);
			info.append(": [");
			for(String val: (String[])session.getParameter(name))
			{
				info.append(val);
				info.append(", ");
			}
			info.append("]\n");
		}
		info.append("}");
		
		//set parameter value
		session.setParameter("PARAMETER1", info.toString());
		//persist session
		try {
			session.sync();
		} catch (InconsistentAppSessionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAppSessionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		response.writeResponseMessage(info.toString());
	}

}
