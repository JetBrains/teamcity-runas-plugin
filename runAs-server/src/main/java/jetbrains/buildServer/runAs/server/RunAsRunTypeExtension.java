package jetbrains.buildServer.runAs.server;

import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.RunTypeExtension;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.util.positioning.PositionAware;
import jetbrains.buildServer.util.positioning.PositionConstraint;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;

public class RunAsRunTypeExtension extends RunTypeExtension implements PositionAware {
  private static final Map<String, String> OurDefaultRunnerProperties = CollectionsUtil.asMap(
      RunAsBean.Shared.getWindowsIntegrityLevelKey(), RunAsBean.Shared.getWindowsIntegrityLevels().get(0).getValue(),
      RunAsBean.Shared.getWindowsLoggingLevelKey(), RunAsBean.Shared.getLoggingLevels().get(0).getValue()
    );

  private final String myViewUrl;
  private final String myEditUrl;

  @Autowired
  public RunAsRunTypeExtension(
    @NotNull final PluginDescriptor descriptor,
    @NotNull final WebControllerManager wcm) {
    myViewUrl = registerView(descriptor, wcm, "runAsView.html", "viewRunAs.jsp");
    myEditUrl = registerView(descriptor, wcm, "runAsEdit.html", "editRunAs.jsp");
  }

  @Override
  @NotNull
  public String getOrderId() {
    return "runAs";
  }

  @Override
  @NotNull
  public PositionConstraint getConstraint() {
    return PositionConstraint.last();
  }

  @Override
  public Collection<String> getRunTypes() {
    return new Collection<String>() {
      @Override
      public int size() {
        return 0;
      }

      @Override
      public boolean isEmpty() {
        return false;
      }

      @Override
      public boolean contains(final Object o) {
        return true;
      }

      @NotNull
      @Override
      public Iterator<String> iterator() {
        return Collections.<String>emptyList().iterator();
      }

      @NotNull
      @Override
      public Object[] toArray() {
        return new Object[0];
      }

      @NotNull
      @Override
      public <T> T[] toArray(@NotNull final T[] a) {
        //noinspection unchecked
        return (T[])new String[0];
      }

      @Override
      public boolean add(final String s) {
        return false;
      }

      @Override
      public boolean remove(final Object o) {
        return false;
      }

      @Override
      public boolean containsAll(@NotNull final Collection<?> c) {
        return true;
      }

      @Override
      public boolean addAll(@NotNull final Collection<? extends String> c) {
        return false;
      }

      @Override
      public boolean removeAll(@NotNull final Collection<?> c) {
        return false;
      }

      @Override
      public boolean retainAll(@NotNull final Collection<?> c) {
        return false;
      }

      @Override
      public void clear() {
      }
    };
  }

  @Nullable
  @Override
  public PropertiesProcessor getRunnerPropertiesProcessor() {
    return new PropertiesProcessor() {
      @Override
      public Collection<InvalidProperty> process(final Map<String, String> properties) {
        final ArrayList<InvalidProperty> result = new ArrayList<InvalidProperty>();

        final String user = properties.get(RunAsBean.Shared.getRunAsUserKey());
        final String password = properties.get(RunAsBean.Shared.getRunAsPasswordKey());

        if(!(StringUtil.isEmpty(user) && StringUtil.isEmpty(password))) {
          if (StringUtil.isEmptyOrSpaces(user)) {
            result.add(new InvalidProperty(RunAsBean.Shared.getRunAsUserKey(), "The user must be specified."));
          }

          if (StringUtil.isEmpty(password)) {
            result.add(new InvalidProperty(RunAsBean.Shared.getRunAsPasswordKey(), "The password must be specified."));
          }
        }

        return result;
      }
    };
  }

  @Override
  public String getEditRunnerParamsJspFilePath() {
    return myEditUrl;
  }

  @Override
  public String getViewRunnerParamsJspFilePath() {
    return myViewUrl;
  }

  @Nullable
  @Override
  public Map<String, String> getDefaultRunnerProperties() {
    return OurDefaultRunnerProperties;
  }

  private String registerView(@NotNull final PluginDescriptor description,
                              @NotNull final WebControllerManager wcm,
                              @NotNull final String url,
                              @NotNull final String jsp) {
    final String actualUrl = description.getPluginResourcesPath(url);
    final String actualJsp = description.getPluginResourcesPath(jsp);

    wcm.registerController(actualUrl, new BaseController() {
      @Override
      protected ModelAndView doHandle(@NotNull final HttpServletRequest request, @NotNull final HttpServletResponse response) throws Exception {
        return new ModelAndView(actualJsp);
      }
    });
    return actualUrl;
  }
}
