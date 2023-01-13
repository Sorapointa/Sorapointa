use std::net::{IpAddr, SocketAddr};

pub trait SocketAddrExt {
    fn divide_into_ip_and_port(&self) -> (Vec<u8>, u16);
}

impl SocketAddrExt for SocketAddr {
    fn divide_into_ip_and_port(&self) -> (Vec<u8>, u16) {
        let ip: Vec<_> = match self.ip() {
            IpAddr::V4(v4) => v4.octets().into(),
            IpAddr::V6(v6) => v6.octets().into(),
        };
        (ip, self.port())
    }
}
