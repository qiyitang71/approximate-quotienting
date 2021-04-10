import sys
import os

with open(sys.argv[1], 'r') as f:
    Lines = f.readlines()
    count = 0
    for line in Lines:
        count += 1
        line = line.rstrip("\n")
        if count  == 1:
            s1,s2 = line.split(' ', 1)
            a,b,c = s2.split('-', 2)
            print(a + "-" + b + ":" + c + ", #states, #transitions, #iter, , #states, #transitions, #iter")
        if count == 3:
            s1,s2 = line.split(' ', 1)
            print("sample model, " + s1 + ", " + s2 + ",  ,  , " + s1 + ", " + s2 + ",  ")
        if count%17 == 6:
            epsilon = line
        if count == 9:
            s1,s2 = line.split(' ', 1)
        if count%17 == 12:
            exactSta, exactTran= line.split(' ', 1)
        if count%17 == 13:
             exactIter = line.split('=', 1)[1]
        if count == 17:
            s3,s4 = line.split(' ', 1)
            print("quotient model, " + s3 + ", " + s4 + ",  ,  , " + s1 + ", " + s2 + ",  ")
        if count%17 == 3 and count != 3:
            sampleSta, sampleTran= line.split(' ', 1)
        if count%17 == 4 and count != 4:
            sampleIter = line.split('=', 1)[1]
            print(epsilon+"")
            print("approximate partition refinement, " + sampleSta + ", " + sampleTran + ", " + sampleIter + ",  , " + exactSta + ", " + exactTran + ", " + exactIter)

f.close()

