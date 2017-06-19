#!/usr/bin/python
import os
f = open("output/APK_CG/cg.txt");
prev = ""
sens_taken =0;
mis_taken = 0;
lines = []
for line in f:
	line = line.rstrip();
	# print newlines
	if(line != ''):
		# print line
		if("----" in line):
			if(sens_taken == 0):
				prev = prev.rstrip()
				# print prev
				lines.append(prev[prev.find("stmt"):])
				sens_taken = 1
			else:
				sens_taken = 0
				continue
		elif("misuse" in line):
			if(mis_taken == 0):
				for l in lines:
					print "sensitive api "+l
				print "global output api"+prev[prev.find("stmt"):]+"\n\n"
				print"--------------------------------------------------"
				print"\n\n"
				mis_taken = 1
			else:
				mis_taken = 0
				continue
		prev = line
