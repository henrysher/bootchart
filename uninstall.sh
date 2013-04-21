#!/bin/sh
# bootchart logger installation
#

TOPDIR=/
MANDIR=$TOPDIR/usr/share/man

# Remove the bootchartd files
rm -f $TOPDIR/sbin/bootchartd
rm -f $TOPDIR/etc/bootchartd.conf

# Remove the man pages
rm -f $MANDIR/man1/bootchart.1
rm -f $MANDIR/man1/bootchartd.1
rm -f $MANDIR/man5/bootchartd.conf.5

# Remove the grub/lilo entry
if [ -x /sbin/grubby ]; then
	title="Bootchart logging"
	grubby --remove-kernel TITLE="$title"
fi
