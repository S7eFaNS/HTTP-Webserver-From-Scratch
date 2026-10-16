package handlers;

import java.io.IOException;

import entities.HttpRequest;
import entities.HttpResponse;
import entities.StatusCodeEnum;
import services.StaticFileService;

public class StaticFileHandler implements Handler{

        @Override
        public HttpResponse handle(HttpRequest request) throws IOException {
            
            String target = request.getPath();

            String filePath = StaticFileService.resolveFilePath(target);

            if(filePath == null){
                return HttpResponse.text(StatusCodeEnum.BAD_REQUEST, "Not found");
            }

            byte[] content = StaticFileService.loadFile(filePath);

            return HttpResponse.bytes(StatusCodeEnum.OK, content, StaticFileService.resolveContentType(filePath));
            

        }

    
}
