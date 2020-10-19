import net.woggioni.jpms.loader.serialization.Serializer;

module A {
    requires B;
    requires serialization;
    requires static lombok;
    uses Serializer;
    exports net.woggioni.jpms.loader.a;
}