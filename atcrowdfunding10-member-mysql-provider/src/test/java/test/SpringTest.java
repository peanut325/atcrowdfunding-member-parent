package test;

import com.atguigu.crowd.CrowdMainClass;
import com.atguigu.crowd.entity.po.MemberPO;
import com.atguigu.crowd.entity.vo.DetailProjectVO;
import com.atguigu.crowd.entity.vo.DetailReturnVO;
import com.atguigu.crowd.entity.vo.PortalProjectVO;
import com.atguigu.crowd.entity.vo.PortalTypeVO;
import com.atguigu.crowd.mapper.MemberPOMapper;
import com.atguigu.crowd.mapper.ProjectPOMapper;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = CrowdMainClass.class)
public class SpringTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private MemberPOMapper memberPOMapper;
    
    @Autowired
    private ProjectPOMapper projectPOMapper;

     private Logger logger = LoggerFactory.getLogger(SpringTest.class);

     @Test
     public void detailReturnVOTest(){
         DetailProjectVO detailProjectVO = projectPOMapper.selectDetailProjectVO(14);
         logger.info(String.valueOf(detailProjectVO.getProjectId()));
         logger.info(detailProjectVO.getProjectName());
         logger.info(detailProjectVO.getProjectDesc());
         logger.info(String.valueOf(detailProjectVO.getFollowerCount()));
         logger.info(String.valueOf(detailProjectVO.getDay()));
         logger.info(String.valueOf(detailProjectVO.getStatus()));
         logger.info(detailProjectVO.getStatusText());
         logger.info(String.valueOf(detailProjectVO.getMoney()));
         logger.info(String.valueOf(detailProjectVO.getPercentage()));
         logger.info(String.valueOf(detailProjectVO.getDeployDate()));
         logger.info(String.valueOf(detailProjectVO.getLastDay()));
         logger.info(String.valueOf(detailProjectVO.getSupporterCount()));
         logger.info(String.valueOf(detailProjectVO.getHeaderPicturePath()));
         List<String> detailPicturePathList = detailProjectVO.getDetailPicturePathList();
         for (String detailPicturePath : detailPicturePathList) {
             logger.info(detailPicturePath);
         }
         List<DetailReturnVO> detailReturnVOList = detailProjectVO.getDetailReturnVOList();
         for (DetailReturnVO detailReturnVO : detailReturnVOList) {
             logger.info(String.valueOf(detailReturnVO.getReturnId()));
             logger.info(String.valueOf(detailReturnVO.getSupportMoney()));
             logger.info(String.valueOf(detailReturnVO.getSignalPurchase()));
             logger.info(String.valueOf(detailReturnVO.getPurchase()));
             logger.info(String.valueOf(detailReturnVO.getSupporterCount()));
             logger.info(String.valueOf(detailReturnVO.getFreight()));
             logger.info(String.valueOf(detailReturnVO.getReturnDate()));
             logger.info(String.valueOf(detailReturnVO.getContent()));

         }
     }

    @Test
    public void connectTest() throws SQLException {
        Connection connection = dataSource.getConnection();
        System.out.println(connection.toString());
    }

    @Test
    public void mybatisTest() {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String source = "123123";
        String encode = passwordEncoder.encode(source);
        MemberPO memberPO = new MemberPO(null, "jack", encode, " 杰 克 ", "jack@qq.com", 1, 1, "杰克", "123123", 2);
        memberPOMapper.insert(memberPO);
    }
    
    @Test
    public void typePOMapperTest(){
        List<PortalTypeVO> portalTypeVOList = projectPOMapper.selectPortalTypeVOList();
        for (PortalTypeVO portalTypeVO : portalTypeVOList) {
            System.out.println(portalTypeVO.getName());
            System.out.println(portalTypeVO.getRemark());
            List<PortalProjectVO> portalProjectVOList = portalTypeVO.getPortalProjectVOList();
            for (PortalProjectVO portalProjectVO : portalProjectVOList) {
                System.out.println(portalProjectVO);
            }
        }
    }

    @Test
    public void testDate(){
        LocalDate todayDate = LocalDate.now();
        System.out.println(todayDate.getDayOfYear());

        String str = "2019-03-03";
        //指定转换格式
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        //进行转换
        LocalDate date = LocalDate.parse(str, fmt);

        System.out.println(date.getDayOfYear());
    }
}
