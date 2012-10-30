package eu.wegov.web.controller;

import eu.experimedia.itinnovation.scc.web.adapters.EMClient;
import java.sql.Timestamp;
import java.util.Date;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.wegov.coordinator.web.Widget;
import eu.wegov.coordinator.web.WidgetDataAsJson;
import eu.wegov.coordinator.web.WidgetSet;
import eu.wegov.web.security.WegovLoginService;

@Controller
@RequestMapping("/home/widgets")
public class WidgetsController {

    @Autowired
    @Qualifier("wegovLoginService")
    WegovLoginService loginService;

    @Autowired
    @Qualifier("EMClient")
    EMClient emClient;

    @RequestMapping(method = RequestMethod.GET, value = "/getWidget/do.json")
    public @ResponseBody
    Widget getWidget(@RequestParam("wId") int wId) {

        Widget theWidget = null;
        try {
            theWidget = loginService.getWidget(wId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return theWidget;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getWidgetData/do.json")
    public @ResponseBody
    WidgetDataAsJson[] getWidgetData(@RequestParam("wId") int wId) {

        WidgetDataAsJson[] result = null;
        try {
            result = loginService.getWidgetData(wId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return result;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getWidgetsMatchingDataType/do.json")
    public @ResponseBody
    Widget[] getWidgetsMatchingDataType(@RequestParam("dataType") String dataType) {

        Widget[] result = null;
        try {
            result = loginService.getWidgetsMatchingDataType(dataType);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return result;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getWidgetsMatchingWidgetType/do.json")
    public @ResponseBody
    Widget[] getWidgetsMatchingWidgetType(@RequestParam("widgetType") String widgetType) {

        Widget[] result = null;
        try {
            result = loginService.getWidgetsMatchingWidgetType(widgetType);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return result;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getWidgetsMatchingWidgetCategory/do.json")
    public @ResponseBody
    Widget[] getWidgetsMatchingWidgetCategory(@RequestParam("widgetCategory") String widgetCategory) {

        Widget[] result = null;
        try {
            result = loginService.getWidgetsMatchingWidgetCategory(widgetCategory);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return result;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/saveWidgetData/do.json")
    public @ResponseBody
    void saveWidgetData(@RequestBody final String inputData) throws Exception {
        try {
            JSONObject inputDataAsJSON = (JSONObject) JSONSerializer.toJSON(inputData);
            int wId = inputDataAsJSON.getInt("wId");
            String type = inputDataAsJSON.getString("type");
            String location = inputDataAsJSON.getString("location");
            String dataAsJson = inputDataAsJSON.getString("data");
            System.out.println("Saving data for widget [" + wId + "]");
            System.out.println("Data: " + dataAsJson);
            String name = inputDataAsJSON.getString("name");
            Timestamp collected_at = new Timestamp((new Date()).getTime());
            loginService.saveWidgetDataAsJson(wId, type, name, location,
                    dataAsJson, collected_at);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getDefaultWidgetSetForPM/do.json")
    public @ResponseBody
    WidgetSet getDefaultWidgetSetForPM() {

        WidgetSet theWidgetSet = null;
        try {
            theWidgetSet = loginService.getDefaultWidgetSetForPM();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return theWidgetSet;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getVisibleWidgetsForDefaultWidgetSet/do.json")
    public @ResponseBody
    Widget[] getVisibleWidgetsForDefaultWidgetSet() {

        Widget[] theWidgets = null;
        try {
            theWidgets = loginService.getVisibleWidgetsForDefaultWidgetSet();
            // for (Widget widget : theWidgets) {
            // System.out.println("[" + widget.getId() + "]" +
            // widget.getColumnName() + ", " + widget.getColumnOrderNum());
            // }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return theWidgets;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getHiddenWidgetsForDefaultWidgetSet/do.json")
    public @ResponseBody
    Widget[] getHiddenWidgetsForDefaultWidgetSet() {

        Widget[] theWidgets = null;
        try {
            theWidgets = loginService.getHiddenWidgetsForDefaultWidgetSet();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return theWidgets;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getWidgetsForDefaultWidgetSet/do.json")
    public @ResponseBody
    Widget[] getWidgetsForDefaultWidgetSet() {

        Widget[] theWidgets = null;
        try {
            theWidgets = loginService.getWidgetsForDefaultWidgetSet();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return theWidgets;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getWidgetSetsForPM/do.json")
    public @ResponseBody
    WidgetSet[] getWidgetSetsForPM() {

        WidgetSet[] allWidgetSets = null;
        try {
            allWidgetSets = loginService.getWidgetSetsForPM();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return allWidgetSets;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getWidgetsForWidgetSet/do.json")
    public @ResponseBody
    Widget[] getWidgetsForWidgetSet(@RequestParam("wsId") int wsId) {

        Widget[] allWidgets = null;
        try {
            allWidgets = loginService.getWidgetsForWidgetSet(wsId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return allWidgets;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getTemplateWidgets/do.json")
    public @ResponseBody
    Widget[] getTemplateWidgets() {

        Widget[] theWidgets = null;
        try {
            theWidgets = loginService.getTemplateWidgets();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return theWidgets;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getTemplateWidgetsMatchingWidgetType/do.json")
    public @ResponseBody
    Widget[] getTemplateWidgetsMatchingWidgetType(@RequestParam("widgetType") String widgetType) {

        Widget[] result = null;
        try {
            result = loginService.getTemplateWidgetsMatchingWidgetType(widgetType);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return result;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getTemplateWidgetsMatchingWidgetCategory/do.json")
    public @ResponseBody
    Widget[] getTemplateWidgetsMatchingWidgetCategory(@RequestParam("widgetCategory") String widgetCategory) {

        Widget[] result = null;
        try {
            result = loginService.getTemplateWidgetsMatchingWidgetCategory(widgetCategory);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return result;
    }

    // int wsId, String columnName, int columnOrderNum, int pmId, String name,
    // String description, String type, String datatype, String dataAsString,
    // String parametersAsString, int isVisible
    @RequestMapping(method = RequestMethod.GET, value = "/duplicateWidget/do.json")
    public @ResponseBody
    void duplicateWidget(@RequestParam("wId") int wId,
            @RequestParam("parametersAsString") String parametersAsString) {

        try {
            loginService.duplicateWidget(wId, parametersAsString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // int wsId - widget ID to duplicate
    // return widgetId (+ve integer number) or -1 if there is an error
    @RequestMapping(method = RequestMethod.GET, value = "/duplicateWidgetToCallingUserDefaultSet/do.json")
    public @ResponseBody
    int duplicateWidgetToCallingUserDefaultSet(@RequestParam("wId") int wId,
            @RequestParam("parametersAsString") String parametersAsString) {

        try {
            int newWidgetId = loginService.duplicateWidgetToCallingUserDefaultSet(wId, parametersAsString);
            
            return newWidgetId;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

//   public void duplicateWidgetToNewWidgetSet(int wId, String parametersAsString, int targetWsId) throws SQLException {
    @RequestMapping(method = RequestMethod.GET, value = "/deleteWidget/do.json")
    public @ResponseBody
    void deleteWidget(@RequestParam("wId") int wId) {

        try {
            loginService.deleteWidget(wId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/updateWidgetParameters/do.json")
    public @ResponseBody
    void updateWidgetParameters(@RequestParam("wId") int wId,
            @RequestParam("newParametersValue") String newParametersValue)
            throws Exception {
        loginService.updateWidgetParameters(wId, newParametersValue);
        
        emClient.pushData("Updated parameters for widget [" + Integer.toString(wId) + "]: " + newParametersValue);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/hideWidget/do.json")
    public @ResponseBody
    void hideWidget(@RequestParam("wId") int wId) throws Exception {
        loginService.hideWidget(wId);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/showWidget/do.json")
    public @ResponseBody
    void showWidget(@RequestParam("wId") int wId) throws Exception {
        loginService.showWidget(wId);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/updateWidgetPositions/do.json")
    public @ResponseBody
    boolean updateWidgetPositions(
            @RequestParam("columnName") String columnName,
            @RequestParam("newOrder") String newOrder) {
        boolean result = false;

        // @RequestParam("wsId") int wsId,
        // Widget[] allWidgets = null;
        try {
            // allWidgets = loginService.getWidgetsForWidgetSetColumn(wsId,
            // columnName);
            // System.out.println("Widgets to be updated for column: " +
            // columnName + ", with new order: " + newOrder);
            // System.out.println("Existing order:");

            // for (Widget widget : allWidgets) {
            // System.out.println(widget.getId() + ": " +
            // widget.getColumnOrderNum());
            // }

            // System.out.println("New order:");
            if (newOrder.length() > 0) {
                String[] newOrderAsArray = newOrder.split(",");

                for (int newOrderNum = 0; newOrderNum < newOrderAsArray.length; newOrderNum++) {
                    int widgetId = Integer.parseInt(newOrderAsArray[newOrderNum]);
                    // System.out.println("Widget with id " + widgetId +
                    // " will appear in position: " + newOrderNum);
                    loginService.updateOrderAndColumnOfWidgetWithId(widgetId,
                            columnName, newOrderNum);
                }
            }

            // System.out.println("Order now in the database for column " +
            // columnName + ":");
            // allWidgets = loginService.getWidgetsForWidgetSetColumn(wsId,
            // columnName);
            // for (Widget widget : allWidgets) {
            // System.out.println(widget.getId() + ": " +
            // widget.getColumnOrderNum());
            // }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
