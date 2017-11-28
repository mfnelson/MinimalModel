



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



makeCharVid = function(nRows, nCols, interval, sizeFactor, dat1, vid.name, pch = 22, col = 1, bg = gray(0.4, alpha = 0.2)){
  ani.options(interval = interval, ani.width = sizeFactor * (dim(dat1[[1]])[3]), ani.height = sizeFactor * (dim(dat1[[1]])[2]), ffmpeg = "C:/Program Files/ffmpeg/bin/ffmpeg.exe")
  saveVideo(expr = {
    for(year in 1: (dim(dat1[[1]])[1] - 1)){
      df = dat1[[2]][[year]]
      par(mar = c(0, 0, 0, 0)); plot(df$x, df$y, cex = 2 * log(df$val + 1), pch = pch, xlim = c(0, 1), ylim = c(0, 1), col = col, bg = bg, lwd = 2)
      par("usr" = c(0, 1, 0, 1))
      abline(h = seq(par("usr")[1], par("usr")[2], length.out = nRows + 1), lwd = 0.1, col = gray(0, alpha = 0.2))
      abline(v = seq(par("usr")[3], par("usr")[4], length.out = nCols + 1), lwd = 0.1, col = gray(0, alpha = 0.2))
      print(paste0("year = ", year))
    }
  }, video.name = vid.name, 
  other.opts = " -c:v libx264 -strict -2 -c:a aac -ar 44100 -preset veryslow -pix_fmt yuv420p -crf 24"
  )
}

makePixelVid = function(interval, sizeFactor, dat1, vid.name){
  ani.options(interval = interval, ani.width = sizeFactor * (dim(dat1[[1]])[3]), ani.height = sizeFactor * (dim(dat1[[1]])[2]), ffmpeg = "C:/Program Files/ffmpeg/bin/ffmpeg.exe")
  saveVideo(expr = {
    for(year in 1: (dim(dat1[[1]])[1] - 1)){
      par(mar = c(0, 0, 0, 0)); image(t(dat1[[1]][year , , ]), axes = F, zlim = c(-0.5, 1))
      par("usr" = c(0, 1, 0, 1))
      abline(h = seq(par("usr")[1], par("usr")[2], length.out = nRows + 1), lwd = 0.1, col = gray(0, alpha = 0.2))
      abline(v = seq(par("usr")[3], par("usr")[4], length.out = nCols + 1), lwd = 0.1, col = gray(0, alpha = 0.2))
      print(paste0("year = ", year))
    }
  }, video.name = vid.name, 
  other.opts = " -c:v libx264 -strict -2 -c:a aac -ar 44100 -preset veryslow -pix_fmt yuv420p -crf 24"
  )
}

