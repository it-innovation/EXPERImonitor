#ifndef SIMPLEAMQPCLIENT_CONFIG_H_
#define SIMPLEAMQPCLIENT_CONFIG_H_

// strerror_s on win32
#define HAVE_STRERROR_S

// strerror_r on linux
/* #undef HAVE_STRERROR_R */


// winsock2.h
#define HAVE_WINSOCK2_H

// sys/socket.h
/* #undef HAVE_SYS_SOCKET_H */

#endif // SIMPLEAMQPCLIENT_CONFIG_H_
