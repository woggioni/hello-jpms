module A {
    requires B;
    requires transitive serialization;
    uses net.woggioni.hello.jpms.serialization.Serializer;
    exports net.woggioni.hello.jpms.a;
}