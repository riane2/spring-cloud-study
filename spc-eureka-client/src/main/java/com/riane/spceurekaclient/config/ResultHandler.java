
package com.riane.spceurekaclient.config;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * @author 时智超
 * @ClassName: ResultHandler
 * @Description: 对结果进行统一封装
 * @date 2018/5/16 16:29
 */
@RestControllerAdvice
public class ResultHandler implements ResponseBodyAdvice<Object> {

    /**
     * @param returnType
     * @param converterType
     * @return
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // TODO Auto-generated method stub
        // 根据业务和上面各个参数判断，是否执行下面的beforeBodyWrite方法，返回true则执行，否则不执行

        if (returnType.getMethod().toString().indexOf(".swagger") != -1) {
            return false;
        }
        return true;
    }


    /**
     * 结果进行统一封装
     *
     * @param body
     * @param returnType
     * @param selectedContentType
     * @param selectedConverterType
     * @param request
     * @param response
     * @return
     */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {

        Result ret = new Result();
        if (!(body instanceof Result)) {
            HttpMethod requestMethod = request.getMethod();
            if (requestMethod == HttpMethod.POST || requestMethod == HttpMethod.PUT || requestMethod == HttpMethod.PATCH) {
                ret.setResutCode(ResultCodeEnum.CREATEORUPDATESUCCESS.getCode(), ResultCodeEnum.CREATEORUPDATESUCCESS.getName());
            } else if (requestMethod == HttpMethod.DELETE) {
                ret.setResutCode(ResultCodeEnum.DELETESUCCESS.getCode(), ResultCodeEnum.DELETESUCCESS.getName());
            } else if (requestMethod == HttpMethod.GET) {
                ret.setResutCode(ResultCodeEnum.NORMAL.getCode(), ResultCodeEnum.NORMAL.getName());
            }
            ret.setData(body);
        } else {
            ret = (Result) body;
        }
        return ret;
    }

}
