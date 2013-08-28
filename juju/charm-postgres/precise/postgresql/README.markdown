
# Postgresql Service


## During db-relation-joined,


### the postgresql service provides:

- `host`
- `user`
- `database`
- `password`

### and requires

- `ip`: deprecated way to specify the client ip address to enable
        access from. This is no longer necessary, you can rely on the
        implicit 'private-address' relation component.

Here's an example client hook providing that

    #!/bin/sh
    relation-set ip=`unit-get private-address`


## During db-relation-changed,

### provides

### accepts

- `ip`: the client ip address to enable access

