import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import javax.script.*;

public class Controller {
    private Object model;
    private Class<?> modelClass;
    private Map<String, double[]> bindFields = new LinkedHashMap<>();
    private int LL;
    private List<String> lataYears = new ArrayList<>();
    private ScriptEngine scriptEngine;

    public Controller(String modelName) throws Exception {
        try {
            modelClass = Class.forName(modelName);
        } catch (ClassNotFoundException e) {
            throw e;
        }
        model = modelClass.getDeclaredConstructor().newInstance();
        initializeBindings();
        ScriptEngineManager manager = new ScriptEngineManager();
        scriptEngine = manager.getEngineByName("groovy");

    }

    private void initializeBindings() throws IllegalAccessException {
        Field[] fields = modelClass.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Bind.class)) {
                field.setAccessible(true);
                if (field.getType() == int.class) {
                    LL = field.getInt(model);
                } else if (field.getType() == double[].class) {
                    bindFields.put(field.getName(), null);
                }
            }
        }
    }

    public Controller readDataFrom(String fname) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(fname));
        String line;
        Map<String, List<Double>> dataMap = new HashMap<>();

        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split("\\s+");
            if (parts[0].equals("LATA")) {
                lataYears = Arrays.asList(Arrays.copyOfRange(parts, 1, parts.length));
                LL = lataYears.size();
                Field llField = modelClass.getDeclaredField("LL");
                llField.setAccessible(true);
                llField.setInt(model, LL);
            } else {
                String varName = parts[0];
                List<Double> values = new ArrayList<>();
                for (int i = 1; i < parts.length; i++) {
                    values.add(Double.parseDouble(parts[i]));
                }
                dataMap.put(varName, values);
            }
        }
        reader.close();

        for (String fieldName : bindFields.keySet()) {
            Field field = modelClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            double[] array = new double[LL];
            field.set(model, array);
            bindFields.put(fieldName, array);
        }

        for (Map.Entry<String, List<Double>> entry : dataMap.entrySet()) {
            String varName = entry.getKey();
            List<Double> values = entry.getValue();
            if (bindFields.containsKey(varName)) {
                double[] array = bindFields.get(varName);
                for (int i = 0; i < LL; i++) {
                    if (i < values.size()) {
                        array[i] = values.get(i);
                    } else {
                        array[i] = array[i - 1];
                    }
                }
            }
        }
        return this;
    }

    public Controller runModel() throws Exception {
        modelClass.getMethod("run").invoke(model);
        // The model may assign new arrays (e.g. PKB = new double[LL]),
        // so read the bound fields back instead of keeping the old references.
        for (String fieldName : new ArrayList<>(bindFields.keySet())) {
            Field field = modelClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            bindFields.put(fieldName, (double[]) field.get(model));
        }
        return this;
    }

    public Controller runScript(String script) throws ScriptException{
        Bindings bindings = scriptEngine.createBindings();
        bindings.put("LL", LL);
        bindings.putAll(bindFields);
        scriptEngine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
        scriptEngine.eval(script);

        for (String key : bindings.keySet()) {
            if (!isSingleLowercase(key) && !bindFields.containsKey(key)) {
                Object obj = scriptEngine.get(key);
                if (obj instanceof double[]) {
                    bindFields.put(key, (double[]) obj);
                }
            }
        }
        return this;
    }

    private static String format(double value) {
        // Ratios and growth rates need more precision than absolute values.
        String pattern = Math.abs(value) < 10 ? "%.3f" : "%.1f";
        return String.format(Locale.ROOT, pattern, value);
    }

        private boolean isSingleLowercase(String varName) {
        return varName.matches("^[a-z]$");
    }

    public Controller runScriptFromFile(String fname) throws IOException, ScriptException, IllegalAccessException, NoSuchFieldException {
        BufferedReader reader = new BufferedReader(new FileReader(fname));
        StringBuilder script = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            script.append(line).append("\n");
        }
        reader.close();
        return runScript(script.toString());
    }

    public String getResultsAsTsv() throws Exception {
        StringBuilder tsv = new StringBuilder();
        tsv.append("LATA");
        for (String year : lataYears) {
            tsv.append("\t").append(year);
        }
        tsv.append("\n");
        for (Map.Entry<String, double[]> entry : bindFields.entrySet()) {
            tsv.append(entry.getKey());
            for (double value : entry.getValue()) {
                tsv.append("\t").append(format(value));
            }
            tsv.append("\n");
        }
        return tsv.toString();
    }
}
