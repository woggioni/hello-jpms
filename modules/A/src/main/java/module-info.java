import net.woggioni.hello.jpms.serialization.Serializer;

module A {
    requires B;
    requires serialization;
    requires static lombok;
    uses Serializer;
    exports net.woggioni.hello.jpms.a;
}