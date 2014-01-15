#dddutils-remote

A lightweight and scalable web application framework.

###Motivation

When I have a new idea, I just want to start coding and put it online ASAP. Laterly, if the idea comes to a real money-maker project, I can still easily scale it for handling the load.

I believe that Java EE is really good for people make stuff standard and portable (at least between the enterprise edition application servers), but I don't think that it is efficient way for people to make things creative.

###Features

- Application sandbox design: each application has an isolated running setup (library, configuration etc)
- Modular app session architecture: services and parameters dynamically installed and uninstalled.
- App session replication: implemented iva different infrastructures (MySQL, MongoDB, Network etc).
- Network I/O engineered with Netty.


The current version is still under development, not ready for production yet.

http://astroperfume.com is the website I created running on dddutils-remote.