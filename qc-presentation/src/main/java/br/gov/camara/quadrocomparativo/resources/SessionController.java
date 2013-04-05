package br.gov.camara.quadrocomparativo.resources;

import javax.servlet.http.HttpServletRequest;

public class SessionController {

	@SuppressWarnings("unchecked")
	public static <T> T get(HttpServletRequest request, String id, Class<T> c){
		return (T)request.getAttribute(c.getSimpleName()+"_"+id);
	}
	
	public static void save(HttpServletRequest request, String id, Object object){
		if (object!= null){
			request.setAttribute(object.getClass().getSimpleName()+"_"+id, object);
		}
	}
	
	public static void delete(HttpServletRequest request, String id, Class<?> c){
		request.removeAttribute(c.getSimpleName()+"_"+id);
	}
	
}
