# mybatis_homework

一、 Mybatis动态SQL是做什么的？都有哪些动态SQL？简述一下动态SQL的执行原理吗？
	答：
		1、 mybatis的动态sql，是在xml文件中以xml标签的形式编写动态sql，完成逻辑判断和动态拼接sql的功能。
    	2、 mybatis提供了9种动态标签：<if/>、<choose/>、<when/>、<otherwise/>、<trim/>、<when/>、<set/>、<foreach/>、<bind/>。
    	3、 使用OGNL的表达式，从SQL参数对象中计算表达式的值，根据表达的值动态拼装SQL,以此来完成动态SQL功能。
二、 Mybatis是否支持延迟加载？如果支持，它的实现原理是什么？
	答、
		1、 mybatis仅支持association(一对一)关联对象和collection（一对多）关联集合对象的延迟加载，在配置文件中，
		   可以配置是否启用延迟加载 lazyLoadingEnabled = true | false。
		2、 它的原理是，使用CGLIB创建目标对象的代理对象，当调用目标方法时，进入拦截器方法，比如调用a.getB().getName()，
		   拦截器invoke()方法发现a.getB()是null值，那么就会单独发送事先保存好的查询关联B对象的sql，把B查询上来，然后调用a.setB(b)，
		   于是a的对象b属性就有值了，接着完成a.getB().getName()方法的调用。这就是延迟加载的基本原理。
三、 Mybatis都有哪些Executor执行器？它们之间的区别是什么？
	答、
		1、 mybatis有三种基本的Executor执行器：SimpleExecutor、ReuseExecutor、BatchExecutor
		2、 SimpleExecutor：每执行一次update或select，就开启一个Statement对象，用完立刻关闭Statement对象。
		   ReuseExecutor：执行update或select，以sql作为key查找Statement对象，存在就使用，不存在就创建，用完后，不关闭Statement对象，
		                  而是放置于Map内，供下一次使用。简言之，就是重复使用Statement对象。
		   BatchExecutor：执行update（没有select，JDBC批处理不支持select），将所有sql都添加到批处理中（addBatch()），等待统一执行（executeBatch()），
		                  它缓存了多个Statement对象，每个Statement对象都是addBatch()完毕后，等待逐一执行executeBatch()批处理。与JDBC批处理相同。
四、 简述下Mybatis的一级、二级缓存（分别从存储结构、范围、失效场景。三个方面来作答）？
	答： 
		一级缓存实际上是一个HashMap 进行的本地缓存，在query的时候，会判断缓存是否存在，不存在则从数据库读取并更新缓存。
		当发生update和commit的时候会被清除。每个SqlSession有效，存在于（BaseExecutor -> PerpetualCache）
		二级缓存是实现的全局缓存，cacheEnabled 启动开启二级缓存，被 CachingExecutor 进行包装。如果是使用本地缓存的实现则单应用环境的SqlSession 有效，
		如果Redis，memcache等则 服务器配置的 Sqlsession 获取Cache命名一致的均有效。当update操作会标记 clearOnCommit，commit的时候会调用二级缓存实现的clear。
		当Update 之后，发生当select 会将数据记录到 entriesToAddOnCommit (发生update之前的则会清除)。 在commit 清空所有缓存之后，会从entriesToAddOnCommit进行还原后面的缓存。
五、简述Mybatis的插件运行原理，以及如何编写一个插件？		
	答：
		Mybatis插件，实际上是一个Java 接口的动态代理实现。通过配置插件，层层代理指定需要被增强的接口中的方法。

		ParameterHandler 参数设置处理
		ResultSetHandler 结果封装处理
		StatementHandler SQL语法构建
		interceptorChain.pluginAll(statementHandler); 获得最终的代理对象

		// 实现一个插件
		@Intercepts(
			@Signature(type = StatementHandler.class ,method="query" ,
			args= {Statement.class, ResultHandler.class})
		)
		public class MyPlugin implements Interceptor {
  		@Override
  		public Object intercept(Invocation invocation) throws Throwable {
		
    		try {
      			System.out.println("接着我就开始执行了");
	    		return invocation.proceed();  
    		} finally {
      			System.out.println("然后我就执行结束了");
   			}
    
  		}
  		@Override
  		public Object plugin(Object target) {
    		return  Plugin.wrap(target, this);
 		}
  
  		public void setProperties(Properties properties) {  }

  		<!-- 配置一下 -->
		<plugins>
  			<plugin interceptor="org.ns.learn.mybatis.MyPlugin"/>
		</plugins>

}

二、编程题

请完善自定义持久层框架IPersistence，在现有代码基础上添加、修改及删除功能。【需要采用getMapper方式】

举例说明：
public interface UserDao {

    public List<User> findAll();

    public User findCondition(User user);

    public int update(User user);

    public int insert(User user);

    public int delete(User id);
}

使用：

	   UserDao userDao;
    @Before
    public void before() throws PropertyVetoException, DocumentException {
        InputStream resourceAsSteam = Resources.getResourceAsSteam("sqlMapConfig.xml");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourceAsSteam);
        SqlSession sqlSession = sqlSessionFactory.openSession();
        userDao = sqlSession.getMapper(UserDao.class);
    }
    
    @Test
    public void test1(){
        User user = new User();
        user.setId(3);
        user.setUsername("ronghua11");
        user.setPassword("123456");
        int update = userDao.update(user);
        System.out.println(update);
    }

    @Test
    public void test2(){
        User user = new User();
        user.setId(4);
        user.setUsername("test");
        user.setPassword("111");
        user.setBirthday("2020-10-10");
        int insert = userDao.insert(user);
        System.out.println(insert);
    }

    @Test
    public  void test3(){
        User user = new User();
        user.setId(4);
        int delete = userDao.delete(user);
        System.out.println(delete);
    }
   
