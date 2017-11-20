# install.packages("ncdf4")
rm(list = ls())

require(ncdf4)
require(animation)

p = "C:/github/MinimalModel/output"
if(dir.exists(p)) outputDir = p
setwd(outputDir)


readNetCdfToArrays = function(dataDir, simName, nRows, nCols){
  
  filenames = array(dim = c(nRows, nCols))
  for(row in 1:nRows) for(col in 1:nCols){
    filenames[row, col] = paste0(
      dataDir, "/", simName, "_row_", row - 1, "_col_", col - 1, ".nc")
  }
  
  datNC = list()
  for(row in 1:nRows){
    datCol = list()
    for(col in 1:nCols){
      filename = filenames[row, col]
      ncf = nc_open(filename)
      dat = ncvar_get(ncf, "beetles")
      datCol[[col]] = dat
      nc_close(ncf)
    }
    datNC[[row]] = datCol
  }
  
  (dataDims = dim(datNC[[1]][[1]]))
  
  # ncvar_get reverses the dimensions so we have (year, col, row)
  datAll = array(dim = c(dataDims[1], nRows * dataDims[3], nCols * dataDims[2]))    
  datNonzero = list()
    
   dim(datAll)
  (nRowsCell = dataDims[3])
  (nColsCell = dataDims[2])
  
  dat1 = matrix(0, nrow = dim(datAll)[2], ncol = dim(datAll)[3])
  for(year in 1:(dataDims[1] - 0)){
    print(paste0("year = ", year))
    for(row in 1:nRows) for(col in 1:nCols) {
      rows = (((row - 1) * nRowsCell):(row * nRowsCell - 1) + 1)
      cols = (((col - 1) * nColsCell):(col * nColsCell - 1) + 1)
      dat = t(datNC[[row]][[col]][year , , ])
      
      dat1[rows, cols] = dat
    }
    
    x = c(); y = c(); val = c()
    for(row in 1:nrow(dat1)) for(col in 1:ncol(dat1)){
      if(dat1[row, col] > 0){
        x = c(x, col / ncol(dat1)); y = c(y, row / nrow(dat1)); val = c(val, dat1[row, col])
      }
    }
    datNonzero[[year]] = data.frame(x, y, val)
    maxVals = max(dat1)
    dat1 = dat1 / max(1, maxVals)
    datAll[year , , ] = dat1
  }
  return(list(datAll, datNonzero))
}


nRows = 4; nCols = 4
simName = "fourByFour"
dataDir = paste0(outputDir, "/fourByFour")

nRows = 4; nCols = 5
simName = "fourByFive"
dataDir = paste0(outputDir, "/", simName)

dat1 = readNetCdfToArrays(dataDir, simName, nRows, nCols)
year = 1000
image(dat1[[1]][year, , ])
(df = dat1[[2]][[year]])
par(mar = c(0, 0, 0, 0)); plot(df$x, df$y, cex = 1 * sqrt(df$val + 1), xlim = c(0, 1), ylim = c(0, 1))
abline(h = (1 : (nRows - 1)) * (1 / nRows))
abline(v = (1 : (nCols - 1)) * (1 / nCols))

nRows = 1; nCols = 2
simName = "oneByTwo"
dataDir = paste0(outputDir, "/oneByTwo")


nRows = 2; nCols = 2
simName = "twoByTwo"
dataDir = paste0(outputDir, "/twoByTwo")


year = 700
df = dat1[[2]][[year]]
png("circles.png", width = 2000, height = 2000)
par(mar = c(0, 0, 0, 0)); plot(df$x, df$y, cex = 6 * log(df$val), xlim = c(0, 1), ylim = c(0, 1), axes = F, lwd = 1)
dev.off()

require(raster)
sizeFactor = 5
dim(dat1[[1]])




ani.options(interval = 1 / 29, ani.width = sizeFactor * (dim(dat1[[1]])[3]), ani.height = sizeFactor * (dim(dat1[[1]])[2]), ffmpeg = "C:/Program Files/ffmpeg/bin/ffmpeg.exe")
saveVideo(expr = {
  for(year in 1: (dim(dat1[[1]])[1] - 1)){
  # for(year in 1: 20){
    # par(mar = c(0, 0, 0, 0)); image(t(dat1[[1]][year , , ]), axes = F, zlim = c(-0.5, 1))
    df = dat1[[2]][[year]]
    par(mar = c(0, 0, 0, 0)); plot(df$x, df$y, cex = 2 * log(df$val + 1), pch = 22, xlim = c(0, 1), ylim = c(0, 1), col = 1, bg = gray(0.4, alpha = 0.2), lwd = 2)
    
    par("usr" = c(0, 1, 0, 1))
    # seq(par("usr")[1], par("usr")[2], length.out = nRows + 1)
    abline(h = seq(par("usr")[1], par("usr")[2], length.out = nRows + 1), lwd = 0.1, col = gray(0, alpha = 0.2))
    abline(v = seq(par("usr")[3], par("usr")[4], length.out = nCols + 1), lwd = 0.1, col = gray(0, alpha = 0.2))
    # mtext(side = 3, text = paste0("max val = ", max(datAll[year , , ])), line = -2)
    print(paste0("year = ", year))
  }
}, video.name = "fourByFour_squares_slow.mp4", 
other.opts = " -c:v libx264 -strict -2 -c:a aac -ar 44100 -preset veryslow -pix_fmt yuv420p -crf 24"
# other.opts = "-c:v libx264 -c:a aac -preset veryslow -crf 35"
)

dim(datAll)