#!/bin/sh
#
# bootchart logger installation
#

# $TOPDIR must be mounted during sysinit startup
TOPDIR=/
MANDIR=$TOPDIR/usr/share/man

# Install the bootchartd files
install -m 755 script/bootchartd $TOPDIR/sbin/bootchartd
install -m 755 script/bootchartd.conf $TOPDIR/etc/bootchartd.conf

# Install the man pages
install -D -m 0644 man/bootchart.1 $MANDIR/man1
install -D -m 0644 man/bootchartd.1 $MANDIR/man1
install -D -m 0644 man/bootchartd.conf.5 $MANDIR/man5

# Add a new grub/lilo entry
if [ -x /sbin/grubby ]; then	
	kernel=$(grubby --default-kernel)
	initrd=$(grubby --info=$kernel | sed -n '/^initrd=/{s/^initrd=//p;q;}')
	[ ! -z $initrd ] && initrd="--initrd=$initrd"
	title="Bootchart logging"
	grubby --remove-kernel TITLE="$title"
	grubby --copy-default --add-kernel=$kernel $initrd --args="init=/sbin/bootchartd" --title="$title"
fi
