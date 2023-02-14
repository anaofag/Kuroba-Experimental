/*
 * KurobaEx - *chan browser https://github.com/K1rakishou/Kuroba-Experimental/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.k1rakishou.chan.core.site.sites;

import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.k1rakishou.chan.core.site.ChunkDownloaderSiteProperties;
import com.github.k1rakishou.chan.core.site.Site;
import com.github.k1rakishou.chan.core.site.SiteRequestModifier;
import com.github.k1rakishou.chan.core.site.SiteIcon;
import com.github.k1rakishou.chan.core.site.common.CommonSite;
import com.github.k1rakishou.chan.core.site.common.vichan.VichanActions;
import com.github.k1rakishou.chan.core.site.common.vichan.VichanApi;
import com.github.k1rakishou.chan.core.site.common.vichan.VichanCommentParser;
import com.github.k1rakishou.chan.core.site.common.vichan.VichanEndpoints;
import com.github.k1rakishou.chan.core.site.http.HttpCall;
import com.github.k1rakishou.chan.core.site.parser.CommentParserType;
import com.github.k1rakishou.common.AppConstants;
import com.github.k1rakishou.common.DoNotStrip;
import com.github.k1rakishou.common.KotlinExtensionsKt;
import com.github.k1rakishou.model.data.board.ChanBoard;
import com.github.k1rakishou.model.data.descriptor.BoardDescriptor;
import com.github.k1rakishou.model.data.descriptor.ChanDescriptor;
import com.github.k1rakishou.model.data.descriptor.SiteDescriptor;

import okhttp3.Request;
import org.jetbrains.annotations.NotNull;

import okhttp3.HttpUrl;

import java.util.Map;

@DoNotStrip
public class Chan1500 extends CommonSite {
    private final ChunkDownloaderSiteProperties chunkDownloaderSiteProperties;
    public static final String SITE_NAME = "1500chan";
    public static final SiteDescriptor SITE_DESCRIPTOR = SiteDescriptor.Companion.create(SITE_NAME);

    public static final CommonSiteUrlHandler URL_HANDLER = new CommonSiteUrlHandler() {
        private static final String ROOT = "https://1500chan.org/";

        @Override
        public Class<? extends Site> getSiteClass() {
            return Chan1500.class;
        }

        @Override
        public HttpUrl getUrl() {
            return HttpUrl.parse(ROOT);
        }

        @Override
        public HttpUrl[] getMediaHosts() {
            return new HttpUrl[]{getUrl()};
        }

        @Override
        public String[] getNames() {
            return new String[]{"1500chan"};
        }

        @Override
        public String desktopUrl(ChanDescriptor chanDescriptor, @Nullable Long postNo) {
            if (chanDescriptor instanceof ChanDescriptor.CatalogDescriptor) {
                return getUrl().newBuilder().addPathSegment(chanDescriptor.boardCode()).toString();
            } else if (chanDescriptor instanceof ChanDescriptor.ThreadDescriptor) {
                return getUrl().newBuilder()
                        .addPathSegment(chanDescriptor.boardCode())
                        .addPathSegment("res")
                        .addPathSegment(((ChanDescriptor.ThreadDescriptor) chanDescriptor).getThreadNo() + ".html")
                        .toString();
            } else {
                return null;
            }
        }
    };

    public Chan1500() {
        chunkDownloaderSiteProperties = new ChunkDownloaderSiteProperties(true, true);
    }

    private class Chan1500SiteRequestModifier extends SiteRequestModifier<Site> {
        public static final String BYPASS_COOKIE = "mc=1";

        public Chan1500SiteRequestModifier(Site site, AppConstants appConstants) {
            super(site, appConstants);
        }

        @Override
        public void modifyHttpCall(HttpCall httpCall, Request.Builder requestBuilder) {
            super.modifyHttpCall(httpCall, requestBuilder);
            KotlinExtensionsKt.appendCookieHeader(requestBuilder, BYPASS_COOKIE);
        }

        @Override
        public void modifyThumbnailGetRequest(Site site, Request.Builder requestBuilder) {
            super.modifyThumbnailGetRequest(site, requestBuilder);
            KotlinExtensionsKt.appendCookieHeader(requestBuilder, BYPASS_COOKIE);
        }

        @Override
        public void modifyCatalogOrThreadGetRequest(Site site,  ChanDescriptor chanDescriptor,  Request.Builder requestBuilder) {
            super.modifyCatalogOrThreadGetRequest(site, chanDescriptor, requestBuilder);
            KotlinExtensionsKt.appendCookieHeader(requestBuilder, BYPASS_COOKIE);
        }

        @Override
        public void modifyFullImageHeadRequest(Site site, Request.Builder requestBuilder) {
            super.modifyFullImageHeadRequest(site, requestBuilder);
            KotlinExtensionsKt.appendCookieHeader(requestBuilder, BYPASS_COOKIE);
        }

        @Override
        public void modifyFullImageGetRequest(Site site, Request.Builder requestBuilder) {
            super.modifyFullImageGetRequest(site, requestBuilder);
            KotlinExtensionsKt.appendCookieHeader(requestBuilder, BYPASS_COOKIE);
        }

        @Override
        public void modifyMediaDownloadRequest(Site site, Request.Builder requestBuilder) {
            super.modifyMediaDownloadRequest(site, requestBuilder);
            KotlinExtensionsKt.appendCookieHeader(requestBuilder, BYPASS_COOKIE);
        }

        @Override
        public void modifyArchiveGetRequest(Site site, Request.Builder requestBuilder) {
            super.modifyArchiveGetRequest(site, requestBuilder);
            KotlinExtensionsKt.appendCookieHeader(requestBuilder, BYPASS_COOKIE);
        }

        @Override
        public void modifySearchGetRequest(Site site, Request.Builder requestBuilder) {
            super.modifySearchGetRequest(site, requestBuilder);
            KotlinExtensionsKt.appendCookieHeader(requestBuilder, BYPASS_COOKIE);
        }

        @Override
        public void modifyCaptchaGetRequest(Site site, Request.Builder requestBuilder) {
            super.modifyCaptchaGetRequest(site, requestBuilder);
            KotlinExtensionsKt.appendCookieHeader(requestBuilder, BYPASS_COOKIE);
        }

        @Override
        public void modifyPostReportRequest(Site site, Request.Builder requestBuilder) {
            super.modifyPostReportRequest(site, requestBuilder);
            KotlinExtensionsKt.appendCookieHeader(requestBuilder, BYPASS_COOKIE);
        }

        @Override
        public void modifyLoginRequest(Site site, Request.Builder requestBuilder) {
            super.modifyLoginRequest(site, requestBuilder);
            KotlinExtensionsKt.appendCookieHeader(requestBuilder, BYPASS_COOKIE);
        }

        @Override
        public void modifyGetPasscodeInfoRequest(Site site, Request.Builder requestBuilder) {
            super.modifyGetPasscodeInfoRequest(site, requestBuilder);
            KotlinExtensionsKt.appendCookieHeader(requestBuilder, BYPASS_COOKIE);
        }

        @Override
        public void modifyPagesRequest(Site site, Request.Builder requestBuilder) {
            super.modifyPagesRequest(site, requestBuilder);
            KotlinExtensionsKt.appendCookieHeader(requestBuilder, BYPASS_COOKIE);
        }
    }

    private class Chan1500Endpoints extends VichanEndpoints {

        public Chan1500Endpoints(CommonSite commonSite, String rootUrl, String sysUrl) {
            super(commonSite, rootUrl, sysUrl);
        }

        @NonNull
        @Override
        public HttpUrl thumbnailUrl(BoardDescriptor boardDescriptor, boolean spoiler, int customSpoilers, Map<String, String> arg) {
            String tim = arg.get("tim");
            String ext = arg.get("ext");

            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
            if (!TextUtils.isEmpty(mimeType) && !mimeType.startsWith("image/")) {
                ext = "jpg";
            }

            if (!ext.startsWith(".")) {
                ext = "." + ext;
            }

            return root.builder()
                    .s(boardDescriptor.getBoardCode())
                    .s("thumb")
                    .s(tim + ext)
                    .url();
        }
    }

    @Override
    public void setup() {
        setEnabled(true);
        setName(SITE_NAME);
        setIcon(SiteIcon.fromFavicon(getImageLoaderV2(), HttpUrl.parse("https://1500chan.org/static/favicon.ico")));

        setBoards(
                ChanBoard.create(BoardDescriptor.create(siteDescriptor().getSiteName(), "b"), "Random"),
                ChanBoard.create(BoardDescriptor.create(siteDescriptor().getSiteName(), "bairro"), "Bairrismo"),
                ChanBoard.create(BoardDescriptor.create(siteDescriptor().getSiteName(), "mod"), "Moderação"),
                ChanBoard.create(BoardDescriptor.create(siteDescriptor().getSiteName(), "$"), "Finanças"),
                ChanBoard.create(BoardDescriptor.create(siteDescriptor().getSiteName(), "c"), "Criptomoedas e Câmbio"),
                ChanBoard.create(BoardDescriptor.create(siteDescriptor().getSiteName(), "clt"), "Empregos, Trabalho e RH"),
                ChanBoard.create(BoardDescriptor.create(siteDescriptor().getSiteName(), "jo"), "Jogatina"),
                ChanBoard.create(BoardDescriptor.create(siteDescriptor().getSiteName(), "lan"), "Jogatina Conjunta"),
                ChanBoard.create(BoardDescriptor.create(siteDescriptor().getSiteName(), "mesa"), "RPG e Jogos de Tabuleiro"),
                ChanBoard.create(BoardDescriptor.create(siteDescriptor().getSiteName(), "pol"), "Política"),
                ChanBoard.create(BoardDescriptor.create(siteDescriptor().getSiteName(), "sancti"), "Religião"),
                ChanBoard.create(BoardDescriptor.create(siteDescriptor().getSiteName(), "pc"), "Programação e Computação"),
                ChanBoard.create(BoardDescriptor.create(siteDescriptor().getSiteName(), "cri"), "Criatividade e Arte"),
                ChanBoard.create(BoardDescriptor.create(siteDescriptor().getSiteName(), "arm"), "Armas e Militaria"),
                ChanBoard.create(BoardDescriptor.create(siteDescriptor().getSiteName(), "mago"), "Falha e Aleatoriedade"),
                ChanBoard.create(BoardDescriptor.create(siteDescriptor().getSiteName(), "fvm"), "DIY, gambiarras e projetos"),
                ChanBoard.create(BoardDescriptor.create(siteDescriptor().getSiteName(), "ve"), "Veículos e Transportes"),
                ChanBoard.create(BoardDescriptor.create(siteDescriptor().getSiteName(), "coz"), "Culinária"),
                ChanBoard.create(BoardDescriptor.create(siteDescriptor().getSiteName(), "nat"), "Natureza, Animais e Agricultura"),
                ChanBoard.create(BoardDescriptor.create(siteDescriptor().getSiteName(), "x"), "Ocultismo e Paranormal"),
                ChanBoard.create(BoardDescriptor.create(siteDescriptor().getSiteName(), "jp"), "Japão e Cultura Otaku"),
                ChanBoard.create(BoardDescriptor.create(siteDescriptor().getSiteName(), "hq"), "Quadrinhos (Comics), Animações Ocidentais e Capeshit"),
                ChanBoard.create(BoardDescriptor.create(siteDescriptor().getSiteName(), "mu"), "Música"),
                ChanBoard.create(BoardDescriptor.create(siteDescriptor().getSiteName(), "tvc"), "Televisão e Cinema"),
                ChanBoard.create(BoardDescriptor.create(siteDescriptor().getSiteName(), "esp"), "Futebol e Outros Esportes"),
                ChanBoard.create(BoardDescriptor.create(siteDescriptor().getSiteName(), "jus"), "Direito e Estudos Jurídicos"),
                ChanBoard.create(BoardDescriptor.create(siteDescriptor().getSiteName(), "lang"), "Estudos de Idiomas"),
                ChanBoard.create(BoardDescriptor.create(siteDescriptor().getSiteName(), "lit"), "Literatura"),
                ChanBoard.create(BoardDescriptor.create(siteDescriptor().getSiteName(), "UFC"), "Universidade Federal da Caravela"),
                ChanBoard.create(BoardDescriptor.create(siteDescriptor().getSiteName(), "sig"), "Self Improvement General"),
                ChanBoard.create(BoardDescriptor.create(siteDescriptor().getSiteName(), "fit"), "Fitness"),
                ChanBoard.create(BoardDescriptor.create(siteDescriptor().getSiteName(), "clô"), "Moda, Estética e Viadices"),
                ChanBoard.create(BoardDescriptor.create(siteDescriptor().getSiteName(), "med"), "Medicina e Drogas"),
                ChanBoard.create(BoardDescriptor.create(siteDescriptor().getSiteName(), "muié"), "Muié Vestida"),
                ChanBoard.create(BoardDescriptor.create(siteDescriptor().getSiteName(), "psico"), "Depressão, Relacionamentos e Casos de Família"),
                ChanBoard.create(BoardDescriptor.create(siteDescriptor().getSiteName(), "pr0n"), "*fapfapfap*"),
                ChanBoard.create(BoardDescriptor.create(siteDescriptor().getSiteName(), "tr"), "Neomulheres"),
                ChanBoard.create(BoardDescriptor.create(siteDescriptor().getSiteName(), "arquivo"), "Arquivo")
        );

        setResolvable(URL_HANDLER);

        setConfig(new CommonConfig() {
            @Override
            public boolean siteFeature(SiteFeature siteFeature) {
                return super.siteFeature(siteFeature) || siteFeature == SiteFeature.POSTING;
            }
        });

        setRequestModifier(new Chan1500SiteRequestModifier(this, appConstants));
        setEndpoints(new Chan1500Endpoints(this, "https://1500chan.org", "https://1500chan.org"));
        setActions(new VichanActions(this, getProxiedOkHttpClient(), getSiteManager(), getReplyManager()));
        setApi(new VichanApi(getSiteManager(), getBoardManager(), this));
        setParser(new VichanCommentParser());
    }

    @NotNull
    @Override
    public CommentParserType commentParserType() {
        return CommentParserType.VichanParser;
    }

    @NonNull
    @Override
    public ChunkDownloaderSiteProperties getChunkDownloaderSiteProperties() {
        return chunkDownloaderSiteProperties;
    }
}
