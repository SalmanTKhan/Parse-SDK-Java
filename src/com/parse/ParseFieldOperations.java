package com.parse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * Utility methods to deal with {@link ParseFieldOperation} decoding, both from JSON objects.
 */
/* package */ final class ParseFieldOperations {
    // A map of all known decoders.
    private static Map<String, ParseFieldOperationFactory> opDecoderMap = new HashMap<>();

    private ParseFieldOperations() {
    }

    /**
     * Registers a single factory for a given __op field value.
     */
    private static void registerDecoder(String opName, ParseFieldOperationFactory factory) {
        opDecoderMap.put(opName, factory);
    }

    /**
     * Registers a list of default decoder functions that convert a JSONObject with an __op field,
     * or a Parcel with a op name string, into a ParseFieldOperation.
     */
    static void registerDefaultDecoders() {
        registerDecoder(ParseRelationOperation.OP_NAME_BATCH, new ParseFieldOperationFactory() {
            @Override
            public ParseFieldOperation decode(JSONObject object, ParseDecoder decoder)
                    throws JSONException {
                ParseFieldOperation op = null;
                JSONArray ops = object.getJSONArray("ops");
                for (int i = 0; i < ops.length(); ++i) {
                    ParseFieldOperation nextOp = ParseFieldOperations.decode(ops.getJSONObject(i), decoder);
                    op = nextOp.mergeWithPrevious(op);
                }
                return op;
            }
        });

        registerDecoder(ParseDeleteOperation.OP_NAME, new ParseFieldOperationFactory() {
            @Override
            public ParseFieldOperation decode(JSONObject object, ParseDecoder decoder) {
                return ParseDeleteOperation.getInstance();
            }
        });

        registerDecoder(ParseIncrementOperation.OP_NAME, new ParseFieldOperationFactory() {
            @Override
            public ParseFieldOperation decode(JSONObject object, ParseDecoder decoder) {
                return new ParseIncrementOperation((Number) decoder.decode(object.opt("amount")));
            }
        });

        registerDecoder(ParseAddOperation.OP_NAME, new ParseFieldOperationFactory() {
            @Override
            public ParseFieldOperation decode(JSONObject object, ParseDecoder decoder) {
                return new ParseAddOperation((Collection) decoder.decode(object.opt("objects")));
            }
        });

        registerDecoder(ParseAddUniqueOperation.OP_NAME, new ParseFieldOperationFactory() {
            @Override
            public ParseFieldOperation decode(JSONObject object, ParseDecoder decoder) {
                return new ParseAddUniqueOperation((Collection) decoder.decode(object.opt("objects")));
            }
        });

        registerDecoder(ParseRemoveOperation.OP_NAME, new ParseFieldOperationFactory() {
            @Override
            public ParseFieldOperation decode(JSONObject object, ParseDecoder decoder) {
                return new ParseRemoveOperation((Collection) decoder.decode(object.opt("objects")));
            }
        });

        registerDecoder(ParseRelationOperation.OP_NAME_ADD, new ParseFieldOperationFactory() {
            @Override
            public ParseFieldOperation decode(JSONObject object, ParseDecoder decoder) {
                JSONArray objectsArray = object.optJSONArray("objects");
                List<ParseObject> objectsList = (List<ParseObject>) decoder.decode(objectsArray);
                return new ParseRelationOperation<>(new HashSet<>(objectsList), null);
            }
        });

        registerDecoder(ParseRelationOperation.OP_NAME_REMOVE, new ParseFieldOperationFactory() {
            @Override
            public ParseFieldOperation decode(JSONObject object, ParseDecoder decoder) {
                JSONArray objectsArray = object.optJSONArray("objects");
                List<ParseObject> objectsList = (List<ParseObject>) decoder.decode(objectsArray);
                return new ParseRelationOperation<>(null, new HashSet<>(objectsList));
            }
        });

        registerDecoder(ParseSetOperation.OP_NAME, new ParseFieldOperationFactory() {
            @Override
            public ParseFieldOperation decode(JSONObject object, ParseDecoder decoder) {
                return null; // Not called.
            }
        });
    }

    /**
     * Converts a parsed JSON object into a ParseFieldOperation.
     *
     * @param encoded A JSONObject containing an __op field.
     * @return A ParseFieldOperation.
     */
    static ParseFieldOperation decode(JSONObject encoded, ParseDecoder decoder) throws JSONException {
        String op = encoded.optString("__op");
        ParseFieldOperationFactory factory = opDecoderMap.get(op);
        if (factory == null) {
            throw new RuntimeException("Unable to decode operation of type " + op);
        }
        return factory.decode(encoded, decoder);
    }

    /**
     * Converts a JSONArray into an ArrayList.
     */
    static ArrayList<Object> jsonArrayAsArrayList(JSONArray array) {
        ArrayList<Object> result = new ArrayList<>(array.length());
        for (int i = 0; i < array.length(); ++i) {
            try {
                result.add(array.get(i));
            } catch (JSONException e) {
                // This can't actually happen.
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    /**
     * A function that creates a ParseFieldOperation from a JSONObject or a Parcel.
     */
    private interface ParseFieldOperationFactory {
        ParseFieldOperation decode(JSONObject object, ParseDecoder decoder) throws JSONException;
    }
}
