package lt.terzer.database.wrappers.adapters;

import lt.terzer.database.wrappers.AdapterRegistry;
import lt.terzer.database.wrappers.Serializer;

import java.util.List;

public class ListAdapter implements Serializer<List> {

    private AdapterRegistry adapterRegistry;

    public ListAdapter(AdapterRegistry adapterRegistry){
        this.adapterRegistry = adapterRegistry;
    }

    @Override
    public String dataType() {
        return "TEXT";
    }

    @Override
    public String serialize(List p0) {
        StringBuilder sb = new StringBuilder("'");
        for(int i = 0;i < p0.size();i++){
            sb.append(p0.get(i).toString());
            if(i+1 != p0.size()) {
                sb.append(",");
            }
        }
        sb.append("'");
        return sb.toString();
    }

    @Override
    public List deserialize(String p0) {
        return null;
    }
}