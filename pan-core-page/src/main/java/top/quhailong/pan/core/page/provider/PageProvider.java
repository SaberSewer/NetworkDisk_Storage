package top.quhailong.pan.core.page.provider;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import top.quhailong.pan.core.page.utils.TokenAnalysisUtils;
import top.quhailong.pan.request.AddShareViewCountRequest;
import top.quhailong.pan.response.ShareDTO;
import top.quhailong.pan.response.UserInfoDTO;
import top.quhailong.pan.utils.*;

import java.util.Date;
import java.util.Map;

/**
 * 页面跳转数据处理类
 *
 * @author: quhailong
 * @date: 2019/9/27
 */
@Component
public class PageProvider {
    @Autowired
    private JedisClusterUtil jedisClusterUtil;
    @Autowired
    private TokenAnalysisUtils tokenAnalysisUtils;

    /**
     * 首页跳转数据处理
     *
     * @author: quhailong
     * @date: 2019/9/27
     */
    public String indexHandle() {
        String token = CookieUtils.getCookie("token");
        if (StringUtils.isEmpty(token)) {
            return "login";
        } else {
            return "redirect:http://localhost:8097/disk/home";
        }
    }

    /**
     * 跳转到主页面数据处理
     *
     * @author: quhailong
     * @date: 2019/9/27
     */
    public String homeHandle(Model model) {
        String token = CookieUtils.getCookie("token");
        if (!StringUtils.isEmpty(token)) {
            try {
                if (jedisClusterUtil.isExistKey("LOGOUT:" + token)) {
                    return "login";
                } else {
                    UserInfoDTO userInfoDTO = tokenAnalysisUtils.tokenAnalysis(token);
                    model.addAttribute("name", userInfoDTO.getUserName());
                    return "index";
                }
            } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException exception) {
                exception.printStackTrace();
                return "user";
            }
        }
        return "login";
    }

    /**
     * 跳转到分享管理页面数据处理
     *
     * @author: quhailong
     * @date: 2019/9/27
     */
    public String shareHandle(Model model) {
        String token = CookieUtils.getCookie("token");
        if (!StringUtils.isEmpty(token)) {
            try {
                if (jedisClusterUtil.isExistKey("LOGOUT:" + token)) {
                    return "login";
                } else {
                    UserInfoDTO userInfoDTO = tokenAnalysisUtils.tokenAnalysis(token);
                    model.addAttribute("name", userInfoDTO.getUserName());
                    return "share";
                }
            } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException exception) {
                exception.printStackTrace();
                return "user";
            }
        }
        return "login";
    }

    /**
     * 查看分享页面数据处理
     *
     * @author: quhailong
     * @date: 2019/9/27
     */
    public String sHandle(Model model, String shareId) {
        String token = CookieUtils.getCookie("token");
        if (!StringUtils.isEmpty(token)) {
            try {
                if (jedisClusterUtil.isExistKey("LOGOUT:" + token)) {
                    return "login";
                } else {
                    UserInfoDTO userInfoDTO = tokenAnalysisUtils.tokenAnalysis(token);
                    String getShareUserUrl = "http://localhost:8095/api/share/getshareuser?shareId=" + shareId;
                    String resultString = HttpClientUtils.HttpGet(getShareUserUrl);
                    RestAPIResult restAPIResult = JSONUtils.parseObject(resultString, RestAPIResult.class);
                    Map<String, Object> map = restAPIResult.getRespMap();
                    UserInfoDTO shareUserInfoDTO = JSONUtils.parseObject((String) map.get("userinfo"), UserInfoDTO.class);
                    if (shareUserInfoDTO == null) {
                        model.addAttribute("name", userInfoDTO.getUserName());
                        return "sError";
                    } else if (shareUserInfoDTO.getUserId().equals(userInfoDTO.getUserId())) {
                        model.addAttribute("name", userInfoDTO.getUserName());
                        return "share";
                    }
                    ShareDTO shareDTO = JSONUtils.parseObject((String) map.get("share"), ShareDTO.class);
                    if (shareDTO.getExpiration() != null && shareDTO.getExpiration().getTime() - (new Date().getTime()) < 0) {
                        return "sError";
                    }
                    model.addAttribute("name", userInfoDTO.getUserName());
                    model.addAttribute("shareUser", shareUserInfoDTO.getUserName());
                    if (shareDTO.getMultiWhether() == 1) {
                        model.addAttribute("shareName", shareDTO.getTheme() + "等文件");
                    } else {
                        model.addAttribute("shareName", shareDTO.getTheme() + "文件");
                    }
                    String addShareViewCountUrl = "http://localhost:8095/api/share/addshareview";
                    AddShareViewCountRequest addShareViewCountRequest = new AddShareViewCountRequest();
                    addShareViewCountRequest.setShareId(shareId);
                    HttpClientUtils.httpPostWithJSON(addShareViewCountUrl, JSONUtils.toJSONString(addShareViewCountRequest));
                    model.addAttribute("shareId", shareDTO.getShareId());
                    return "s";
                }
            } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException exception) {
                exception.printStackTrace();
                return "user";
            }
        }
        return "login";
    }
}
