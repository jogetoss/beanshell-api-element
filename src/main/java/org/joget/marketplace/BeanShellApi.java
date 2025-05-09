package org.joget.marketplace;

import org.joget.api.annotations.Operation;
import org.joget.api.annotations.Param;
import org.joget.api.annotations.Response;
import org.joget.api.annotations.Responses;
import org.joget.api.model.ApiDefinition;
import org.joget.api.model.ApiPluginAbstract;
import org.joget.api.model.ApiResponse;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.app.service.AppUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeanShellApi extends ApiPluginAbstract {

    private final static String MESSAGE_PATH = "messages/BeanShellApi";

    @Override
    public String getName() {
        return "BeanShell API Element Plugin";
    }

    @Override
    public String getVersion() {
        return "8.0.0";
    }

    @Override
    public String getDescription() {
        return AppPluginUtil.getMessage("org.joget.beanshellapi.desc", getClassName(), getResourceBundlePath());
    }

    @Override
    public String getIcon() {
        return "<i class=\"fas fa-code\"></i>";
    }

    @Override
    public String getTag() {
        return "beanshell/{scriptId}";
    }

    @Override
    public String getTagDesc() {
        return AppPluginUtil.getMessage("org.joget.beanshellapi.tagDesc", getClassName(), getResourceBundlePath());
    }

    @Override
    public String getLabel() {
        return AppPluginUtil.getMessage("org.joget.beanshellapi.label", getClassName(), getResourceBundlePath());
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/BeanShellApi.json", null, true, getResourceBundlePath());
    }

    @Override
    public String getResourceBundlePath() {
        return MESSAGE_PATH;
    }

    @Operation(
            path = "/",
            type = Operation.MethodType.GET,
            summary = "@@org.joget.beanshellapi.get.summary@@",
            description = "@@org.joget.beanshellapi.desc@@"
    )
    @Responses({
            @Response(responseCode = 200, description = "@@org.joget.beanshellapi.resp.200@@", definition = "Result"),
            @Response(responseCode = 404, description = "@@org.joget.beanshellapi.resp.404@@", definition = "ApiResponse")
    })
    public ApiResponse get(HttpServletRequest request, HttpServletResponse response) {
        return runBeanshell(request, response);
    }

    @Operation(
            path = "/",
            type = Operation.MethodType.POST,
            summary = "@@org.joget.beanshellapi.post.summary@@",
            description = "@@org.joget.beanshellapi.desc@@"
    )
    @Responses({
            @Response(responseCode = 200, description = "@@org.joget.beanshellapi.resp.200@@", definition = "Result"),
            @Response(responseCode = 404, description = "@@org.joget.beanshellapi.resp.404@@", definition = "ApiResponse")
    })
    public ApiResponse post(HttpServletRequest request, HttpServletResponse response) {
        return runBeanshell(request, response);
    }

    private ApiResponse runBeanshell(HttpServletRequest request, HttpServletResponse response){
        String scriptId = getPropertyString("scriptId");
        String script = getPropertyString("script");
        Map properties = new HashMap();
        properties.put("request", request);

        JSONObject results = (JSONObject) AppPluginUtil.executeScript(script, properties);

        if (results != null) {
            // When response code is not provided, will default to 200
            int code = results.has("code") && !results.isNull("code") ? results.getInt("code") : 200;

            // When response message is not provided, will use default error message
            // https://developer.mozilla.org/en-US/docs/Web/HTTP/Status
            if (code >= 400 && code <= 599) {
                String message = results.has("message") && !results.isNull("message")
                    ? results.getString("message")
                    : AppPluginUtil.getMessage("org.joget.beanshellapi.resp.404", getClassName(), getResourceBundlePath());
                
                return new ApiResponse(code, message);
            }

            if (results.has("data") && !results.isNull("data")) {
                Object data = results.get("data");

                if (data instanceof JSONObject) {
                    return new ApiResponse(code, results.getJSONObject("data"));
                } else if (data instanceof JSONArray) {
                    return new ApiResponse(code, results.getJSONArray("data"));
                }
            }
        }
        
        return new ApiResponse(404, AppPluginUtil.getMessage("org.joget.beanshellapi.resp.404", getClassName(), getResourceBundlePath()));
    }

    protected String[] getFields() {
        return new String[]{"result"};
    }

    @Override
    public Map<String, ApiDefinition> getDefinitions() {
        Map<String, ApiDefinition> defs = new HashMap<String, ApiDefinition>();
        defs.put("Result", new ApiDefinition("Result", getFields()));
        return defs;
    }
}
