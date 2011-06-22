/*
 * Copyright (c) 2011 Evolveum
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.opensource.org/licenses/cddl1 or
 * CDDLv1.0.txt file in the source code distribution.
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 *
 * Portions Copyrighted 2011 [name of copyright owner]
 * Portions Copyrighted 2010 Forgerock
 */

package com.evolveum.midpoint.web.util;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.application.ProjectStage;
import javax.faces.context.FacesContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import com.evolveum.midpoint.api.logging.Trace;
import com.evolveum.midpoint.common.result.OperationResult;
import com.evolveum.midpoint.logging.TraceManager;
import com.evolveum.midpoint.web.jsf.messages.MidPointMessage;

/**
 * 
 * @author lazyman
 */
public abstract class FacesUtils {

	public static final String DATE_PATTERN = "EEE, d. MMM yyyy HH:mm:ss.SSS";
	private static final Trace TRACE = TraceManager.getTrace(FacesUtils.class);

	public static String getRequestParameter(String name) {
		if (StringUtils.isEmpty(name)) {
			throw new IllegalArgumentException("Attribute name can't be null.");
		}
		return (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap()
				.get(name);
	}

	public static String translateKey(String key, Object[] arguments) {
		if (arguments == null) {
			return translateKey(key);
		}

		MessageFormat format = new MessageFormat(translateKey(key));
		return format.format(arguments);
	}

	public static String translateKey(String key) {
		if (key == null) {
			throw new IllegalArgumentException("Key can't be null");
		}
		Application application = FacesContext.getCurrentInstance().getApplication();

		String translation = null;
		if (ProjectStage.Development.equals(application.getProjectStage())) {
			translation = "???" + key + "???";
		} else {
			translation = key;
		}

		try {
			ResourceBundle bundle = ResourceBundle.getBundle(application.getMessageBundle(), FacesContext
					.getCurrentInstance().getViewRoot().getLocale());

			translation = bundle.getString(key);
		} catch (Exception ex) {
			TRACE.warn("Couldn't find key '" + key + "', reason: " + ex.getMessage());
		}

		return translation;
	}

	public static void addWarnMessage(String msg) {
		addWarnMessage(msg, null);
	}

	public static void addSuccessMessage(String msg) {
		addSuccessMessage(msg, null);
	}

	public static void addErrorMessage(String msg) {
		addErrorMessage(msg, null);
	}

	public static void addWarnMessage(String msg, Exception ex) {
		addMessage(FacesMessage.SEVERITY_WARN, msg, ex);
	}

	public static void addSuccessMessage(String msg, Exception ex) {
		addMessage(FacesMessage.SEVERITY_INFO, msg, ex);
	}

	public static void addErrorMessage(String msg, Exception ex) {
		addMessage(FacesMessage.SEVERITY_ERROR, msg, ex);
	}

	private static void addMessage(FacesMessage.Severity severity, String msg, Exception ex) {
		FacesMessage message = null;
		if (ex == null) {
			message = new FacesMessage(severity, msg, null);
			FacesContext ctx = FacesContext.getCurrentInstance();
			if (null != ctx) {
				ctx.addMessage(null, message);
			}

			return;
		}

		OperationResult result = new OperationResult("Unknown");
		result.recordFatalError(ex.getMessage(), ex);
		addMessage(result);
	}

	public static void addMessage(OperationResult result) {
		Validate.notNull(result, "Operation result must not be null.");
		FacesMessage.Severity severity = FacesMessage.SEVERITY_WARN;
		switch (result.getStatus()) {
			case FATAL_ERROR:
			case PARTIAL_ERROR:
				severity = FacesMessage.SEVERITY_ERROR;
				break;
			case SUCCESS:
				severity = FacesMessage.SEVERITY_INFO;
				break;
			case UNKNOWN:
			case WARNING:
				severity = FacesMessage.SEVERITY_WARN;
				break;
		}

		MidPointMessage message = new MidPointMessage(severity, result.getMessage(), result);
		FacesContext ctx = FacesContext.getCurrentInstance();
		if (null != ctx) {
			ctx.addMessage(null, message);
		}
	}

	public static String formatDate(Date date) {
		if (date == null) {
			return null;
		}

		DateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);
		return dateFormat.format(date);
	}
}
