(ns teslogger.netty
  (:use [teslogger.plumbing])
  (:import [java.net InetSocketAddress]
           [java.util.concurrent Executors]
           [java.io ByteArrayInputStream]
           [io.netty.bootstrap ServerBootstrap]
           [io.netty.channel ChannelPipeline ChannelInitializer ChannelOption
                             SimpleChannelInboundHandler]
           [io.netty.channel.nio NioEventLoopGroup]
           [io.netty.channel.socket.nio NioServerSocketChannel]
           [io.netty.handler.stream ChunkedWriteHandler]
           [io.netty.handler.codec.http HttpContentCompressor HttpRequestDecoder
                                        HttpResponseEncoder HttpObjectAggregator]))

(defn- make-handler [handler zerocopy]
  (proxy [SimpleChannelInboundHandler] []
    (channelRead0 [ctx req]
           (let [request-map (build-request-map ctx req)
                 ring-response (handler request-map)]
             (when ring-response
               (write-response ctx zerocopy (request-map :keep-alive) ring-response))))))

(defn- pipeline-factory [options handler]
  (proxy [ChannelInitializer] []
     (initChannel [ch]
       (let [pipeline (.pipeline ch)]
         (doto pipeline
           (.addLast "decoder" (HttpRequestDecoder.))
           (.addLast "aggregator" (HttpObjectAggregator. 1048576))
           (.addLast "encoder" (HttpResponseEncoder.))
           (.addLast "chunkedWriter" (ChunkedWriteHandler.))
           (.addLast "handler" (make-handler handler (or (:zerocopy options) false))))))))

(defn- create-server [{:keys [port boss-group worker-group] :or {port 5621} :as options} handler]
  (let [bootstrap (ServerBootstrap.)]
    (doto bootstrap
      (.group boss-group worker-group)
      (.channel NioServerSocketChannel)
      (.localAddress (int port))
      (.option ChannelOption/SO_BACKLOG (int 100))
      (.childOption ChannelOption/TCP_NODELAY true)
      (.childHandler (pipeline-factory options handler)))
    bootstrap))

(defn- bind [bs port]
  (.bind bs (InetSocketAddress. port)))

(defn run-netty [handler options]
  (let [config (assoc options
                 :boss-group (NioEventLoopGroup.)
                 :worker-group (NioEventLoopGroup.))
        bootstrap (create-server config handler)
        channel (.. bootstrap bind sync channel)]
    (fn []
      (try
        (.close channel)
        (finally 
          (.shutdownGracefully (:worker-group config))
          (.shutdownGracefully (:boss-group config)))))))
