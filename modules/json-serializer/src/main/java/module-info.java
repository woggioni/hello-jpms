import net.woggioni.hello.jpms.serialization.Serializer;
import net.woggioni.hello.jpms.serializer.JsonSerializer;

module json.serializer {
    requires static serialization;
    requires static lombok;
    requires com.fasterxml.jackson.databind;
    provides Serializer with JsonSerializer;
}