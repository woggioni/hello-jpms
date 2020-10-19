import net.woggioni.jpms.loader.serialization.Serializer;
import net.woggioni.jpms.loader.serializer.JsonSerializer;

module json.serializer {
    requires static serialization;
    requires static lombok;
    requires com.fasterxml.jackson.databind;
    provides Serializer with JsonSerializer;
}