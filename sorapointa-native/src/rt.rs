use once_cell::sync::OnceCell;
use tokio::runtime::{self, Runtime};

static RUNTIME: OnceCell<Runtime> = OnceCell::new();

pub fn global_runtime() -> &'static Runtime {
    RUNTIME.get_or_init(|| {
        runtime::Builder::new_multi_thread()
            .enable_all()
            .build()
            .expect("Failed to create tokio runtime")
    })
}
