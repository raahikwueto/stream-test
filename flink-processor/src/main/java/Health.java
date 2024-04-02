import java.util.Objects;

public class Health{
    public String name;
    public Double temperature;

    public Health() {}

    public Health(String city, String temperature){
        this.name = name;
        this.temperature = Double.valueOf(temperature);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Name{");
        sb.append("name=").append(name).append('\'');
        sb.append(", temperature=").append(String.valueOf(temperature)).append('\'');
        return sb.toString();
    }

    public int hashCode() {
        return Objects.hash(super.hashCode(), name, temperature);
    }
}

