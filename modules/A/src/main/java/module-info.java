import net.woggioni.hello.jpms.serialization.Serializer;

module A {
    requires B;
    requires serialization;
    uses Serializer;
    exports net.woggioni.hello.jpms.a;
}