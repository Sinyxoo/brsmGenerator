package by.brsm.app.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON-сериализация для полей agenda_items.fields_json и attendees_json.
 */
public final class JsonUtils {

    private static final Gson GSON = new GsonBuilder().create();
    private static final Type MAP_TYPE = new TypeToken<LinkedHashMap<String, Object>>() {
    }.getType();
    private static final Type INT_LIST_TYPE = new TypeToken<List<Integer>>() {
    }.getType();

    private JsonUtils() {
    }

    public static String toJson(Map<String, Object> map) {
        return GSON.toJson(map != null ? map : new LinkedHashMap<>());
    }

    public static Map<String, Object> mapFromJson(String json) {
        if (StringUtils.isBlank(json)) {
            return new LinkedHashMap<>();
        }
        Map<String, Object> map = GSON.fromJson(json, MAP_TYPE);
        return map != null ? map : new LinkedHashMap<>();
    }

    public static String attendeeIdsToJson(List<Integer> ids) {
        return GSON.toJson(ids);
    }

    public static List<Integer> attendeeIdsFromJson(String json) {
        if (StringUtils.isBlank(json)) {
            return List.of();
        }
        List<Integer> ids = GSON.fromJson(json, INT_LIST_TYPE);
        return ids != null ? ids : List.of();
    }
}
