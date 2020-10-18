module example_bundle {
    requires A;
    requires static lombok;
    requires com.fasterxml.jackson.databind;
    exports net.woggioni.jpms.loader.example.bundle;
}