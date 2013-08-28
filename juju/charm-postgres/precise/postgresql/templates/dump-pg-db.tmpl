#!/usr/bin/python

# Copyright 2008-2011 Canonical Ltd.  All rights reserved.

"""
Backup one or more PostgreSQL databases.

Suitable for use in crontab for daily backups.
"""

__metaclass__ = type
__all__ = []

import sys
import os
import os.path
import stat
import logging
import commands
from datetime import datetime
from optparse import OptionParser

MB = float(1024 * 1024)


def main(options, databases):
    #Need longer file names if this is used more than daily
    #today = datetime.now().strftime('%Y%m%d_%H:%M:%S')
    today = datetime.now().strftime('%Y%m%d')

    backup_dir = options.backup_dir
    rv = 0

    for database in databases:
        dest = os.path.join(backup_dir, '%s.%s.dump' % (database, today))

        # base cmd setup; to be modified per the compression desired
        cmd = " ".join([
            "/usr/bin/pg_dump",
            "-U", "postgres",
            # Need to exclude slony schema from dumps, as read lock on
            # event table blocks exclusive lock wanted by slony. And it
            # normally only causes trouble anyway.
            "--exclude-schema=_sl",
            "--format=c",
            "--blobs",
            ])

        # alter the cmd to be used based on compression chosen
        if options.compression_cmd == 'gzip':
            dest = dest + ".gz"
            cmd = " ".join([
                cmd,
                "--compress=0",
                database,
                "| gzip -c%d" % options.compression_level,
                ">", dest,
                ])
        elif options.compression_cmd == 'bzip2':
            dest = dest + ".bz2"
            cmd = " ".join([
                cmd,
                "--compress=0",
                database,
                "| bzip2 -c%d" % options.compression_level,
                ">", dest,
                ])
        elif options.compression_cmd == 'postgres':
            cmd = " ".join([
                cmd,
                "--compress=%d" % options.compression_level,
                "--file=%s" % dest,
                database,
                ])
        else:  # none
            cmd = " ".join([
                cmd,
                "--compress=0",
                "--file=%s" % dest,
                database,
                ])

        # If the file already exists, it is from an older dump today.
        # We don't know if it was successful or not, so abort on this
        # dump. Leave for operator intervention
        if os.path.exists(dest):
            log.error("%s already exists. Skipping." % dest)
            continue

        (rv, outtext) = commands.getstatusoutput(cmd)
        if rv != 0:
            log.critical("Failed to backup %s (%d)" % (database, rv))
            log.critical(outtext)
            continue

        size = os.stat(dest)[stat.ST_SIZE]
        log.info("Backed up %s (%0.2fMB)" % (database, size / MB))

    return rv

if __name__ == '__main__':
    parser = OptionParser(
            usage="usage: %prog [options] database [database ..]"
            )
    parser.add_option("-v", "--verbose", dest="verbose", default=0,
            action="count")
    parser.add_option("-q", "--quiet", dest="quiet", default=0,
            action="count")
    parser.add_option("-d", "--dir", dest="backup_dir",
            default="/var/lib/postgres/backups")
    parser.add_option("-z", "--compression", dest="compression_cmd",
            default="gzip")
    parser.add_option("-l", "--compression-level", type=int,
            dest="compression_level", default=6)
    (options, databases) = parser.parse_args()
    if len(databases) == 0:
        parser.error("must specify at least one database")
    if not os.path.isdir(options.backup_dir):
        parser.error(
                "Incorrect --dir. %s does not exist or is not a directory" % (
                    options.backup_dir
                    )
                )
    valid_compression_cmd = ["gzip", "bzip2", "postgres", "none"]
    if options.compression_cmd not in valid_compression_cmd:
        parser.error(
                "The compression command must be one of: " + " ".join(valid_compression_cmd)
                )
    if options.compression_level < 1 or options.compression_level > 9:
        parser.error(
                "The compression level must be between 1 and 9: %s" %
                options.compression_level
        )

    # Setup our log
    log = logging.getLogger('pgbackup')
    hdlr = logging.StreamHandler(sys.stderr)
    hdlr.setFormatter(logging.Formatter(
            fmt='%(asctime)s %(levelname)s %(message)s'
            ))
    log.addHandler(hdlr)
    verbosity = options.verbose - options.quiet
    if verbosity > 0:
        log.setLevel(logging.DEBUG)
    elif verbosity == 0:  # Default
        log.setLevel(logging.INFO)
    elif verbosity == -1:
        log.setLevel(logging.WARN)
    elif verbosity < -1:
        log.setLevel(logging.ERROR)

    sys.exit(main(options, databases))
