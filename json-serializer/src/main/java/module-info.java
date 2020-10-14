module json.serializer {
    requires static serialization;
    requires static lombok;
    requires com.fasterxml.jackson.databind;
    provides net.woggioni.hello.jpms.serialization.Serializer with net.woggioni.hello.jpms.serializer.JsonSerializer;
}