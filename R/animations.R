# install.packages("ncdf4")
require(ncdf4)
require(animation)

p = "C:/github/MinimalModel/output"
if(dir.exists(p)) setwd(p)


filenames = c()
for(i in 0:1) for(j in 0:1) filenames = c(filenames, paste0("twoByTwo_row_", i, "_col_", j, ".nc"))

i = 1

dat = list()
for(i in 1:4){
  ncf = nc_open(filenames[i])
  dat[[i]] = ncvar_get(ncf, "arrivingBeetles")
  nc_close(ncf)  
}
nrows = dim(dat[[1]])[2]
ncols = dim(dat[[1]])[3] 
nyears = dim(dat[[1]])[1] - 1
dat1 = array(0, dim = c(nyears, 2 * nrows, 2 * ncols))
maxVals = c()
for(year in 1:nyears){
  dat1[year , , ] = cbind(
    rbind(dat[[1]][year, , ], dat[[2]][year, , ]), 
    rbind(dat[[3]][year, , ], dat[[4]][year, , ]))  
  maxVals = c(maxVals, max(dat1[year , , ]))
}

for(year in 1:nyears){
  dat1[year , , ] = dat1[year , , ] / maxVals[year]
}



min(maxVals)
tail(maxVals)
year = 300
par(mar = c(0, 0, 0, 0)); image(dat1[year , , ], axes = F, zlim = c(-0.5, 1))
abline(h = 0.5); abline( v = 0.5)
i = 3



sizeFactor = 6
ani.options(interval = 0.025, ani.width = sizeFactor * (nrows * 2), ani.height = sizeFactor * (ncols * 2), ffmpeg = "C:/Program Files/ffmpeg/bin/ffmpeg.exe")
saveVideo(expr = {
  for(year in 1:nyears){
    par(mar = c(0, 0, 0, 0)); image(dat1[year , , ], axes = F, zlim = c(-0.5, 1))
    abline(h = 0.5); abline( v = 0.5)
    print(paste0("year = ", year))
  }
}, video.name = "two_by_two.mp4", other.opts = "-c:v libx264 -preset ultrafast -crf 0")

